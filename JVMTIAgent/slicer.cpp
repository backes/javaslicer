#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include "jvmti.h"
#include "jni.h"
#include "classfile_constants.h"
#define _STRING(s) #s
#define STRING(s) _STRING(s)
typedef struct {
	/* JVMTI Environment */
	jvmtiEnv *jvmti;
	JNIEnv * jni;
	jboolean vm_is_started;
	jboolean vmDead;

	/* Data access Lock */
	jrawMonitorID lock;
	JavaVM* jvm;
} GlobalAgentData;


static jvmtiEnv *jvmti = NULL;
static jvmtiCapabilities capa;
static GlobalAgentData *gdata;

/* Send message to stdout or whatever the data output location is */
void stdout_message(const char * format, ...) {
	va_list ap;

	va_start(ap, format);
	(void) vfprintf(stdout, format, ap);
	va_end(ap);
}
/* Send message to stderr or whatever the error output location is and exit  */
void fatal_error(const char * format, ...) {
	va_list ap;

	va_start(ap, format);
	(void) vfprintf(stderr, format, ap);
	(void) fflush(stderr);
	va_end(ap);
	exit(3);
}

/* Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
static void check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum,
		const char *str) {
	if (errnum != JVMTI_ERROR_NONE) {
		char *errnum_str;

		errnum_str = NULL;
		(void) jvmti->GetErrorName(errnum, &errnum_str);

		printf("ERROR: JVMTI: %d(%s): %s\n", errnum,
				(errnum_str == NULL ? "Unknown" : errnum_str),
				(str == NULL ? "" : str));
	}
}

/* Enter a critical section by doing a JVMTI Raw Monitor Enter */
static void enter_critical_section(jvmtiEnv *jvmti) {
	jvmtiError error;

	error = jvmti->RawMonitorEnter(gdata->lock);
	check_jvmti_error(jvmti, error, "Cannot enter with raw monitor");
}

/* Exit a critical section by doing a JVMTI Raw Monitor Exit */
static void exit_critical_section(jvmtiEnv *jvmti) {
	jvmtiError error;

	error = jvmti->RawMonitorExit(gdata->lock);
	check_jvmti_error(jvmti, error, "Cannot exit with raw monitor");
}

void describe(jvmtiError err) {
	jvmtiError err0;
	char *descr;
	err0 = jvmti->GetErrorName(err, &descr);
	if (err0 == JVMTI_ERROR_NONE) {
		printf(descr);
	} else {
		printf("error [%d]", err);
	}
}
static int readInt(unsigned char * b, int index) {
	return ((b[index] & 0xFF) << 24) | ((b[index + 1] & 0xFF) << 16)
			| ((b[index + 2] & 0xFF) << 8) | (b[index + 3] & 0xFF);
}
static int readUnsignedShort(unsigned char * b, int index) {
    return ((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF);
}
static int getIntFromCPool(int idxToCpool, unsigned char * cpool)
{
	int v = 0;
	int strlen = 0;
	int idx = 1;
	while (idx < idxToCpool) {
		switch (cpool[v]) {
		case JVM_CONSTANT_Fieldref:
		case JVM_CONSTANT_Methodref:
		case JVM_CONSTANT_InterfaceMethodref:
		case JVM_CONSTANT_Integer:
		case JVM_CONSTANT_Float:
		case JVM_CONSTANT_NameAndType:
		case JVM_CONSTANT_InvokeDynamic:
			v += 5;
			break;
		case JVM_CONSTANT_Long:
		case JVM_CONSTANT_Double:
			v += 9;
			idx++;
			break;
		case JVM_CONSTANT_Utf8:
			strlen = readUnsignedShort(cpool, v + 1);
			v += 3 + strlen;
			break;
		case JVM_CONSTANT_MethodHandle:
		default:
			v += 3;
			break;
		}
		idx++;
	}
	//v now points to the end of the int
	return readInt(cpool, v + 1);
}
static int JNICALL getTraceLocation(JNIEnv *env, jobject obj, jint depth) {
	jvmtiFrameInfo frames[depth + 1];
	jint count;
	jvmtiError err;
	char *methodName;
	char *methodDesc;
	unsigned char *bytecodes;
	jclass declaringClass;
	jint nBytecodes;
	jint bci;
	jint cpoolSize;
	jint cpoolBCSize;
	int u = 0;
	int opcode;
	int len;
	int i;
	int ret = -1;

	unsigned char * cpool;
	static unsigned char _opcode_length[JVM_OPC_MAX + 1] =
	JVM_OPCODE_LENGTH_INITIALIZER;

	err = gdata->jvmti->GetStackTrace(NULL, 2, depth + 1, frames, &count);
	check_jvmti_error(jvmti, err, "Cant get stack trace");

//	err = jvmti->GetMethodName(frames[depth].method, &methodName, &methodDesc, NULL);
//	check_jvmti_error(jvmti, err, "Can't retrieve method name");
	err = jvmti->GetMethodDeclaringClass(frames[depth].method, &declaringClass);
	check_jvmti_error(jvmti, err, "Can't retrieve method Class");
	err = jvmti->GetBytecodes(frames[depth].method, &nBytecodes, &bytecodes);
	check_jvmti_error(jvmti, err, "Can't retrieve method bytecodes");
	err = jvmti->GetConstantPool(declaringClass, &cpoolSize, &cpoolBCSize,
			&cpool);
	check_jvmti_error(jvmti, err, "Can't retrieve class constant pool");

	//now read up to the instruction before this one

	while (u < frames[depth].location - 1) {
		opcode = bytecodes[u];
		if (opcode > JVM_OPC_MAX) {
			fatal_error("Invalid opcode");
		}
		if (opcode == JVM_OPC_tableswitch) {
			u = u + 4 - (u & 3);
			for (int i = readInt(bytecodes, u + 8) - readInt(bytecodes, u + 4)
					+ 1; i > 0; --i) {
				u += 4;
			}
			u += 12;
		} else if (opcode == JVM_OPC_lookupswitch) {
			// skips 0 to 3 padding bytes
			u = u + 4 - (u & 3);
			// reads instruction
			for (int i = readInt(bytecodes, u + 4); i > 0; --i) {
				u += 8;
			}
			u += 8;
		} else {
			len = _opcode_length[opcode];
			if (u + len == frames[depth].location - 1) {
				if (opcode == JVM_OPC_ldc) {
					ret = getIntFromCPool(bytecodes[u + 1] & 0xFF,cpool);
				} else if (opcode == JVM_OPC_ldc_w) {
					ret = getIntFromCPool(readUnsignedShort(bytecodes, u + 1),cpool);
				} else if (opcode == JVM_OPC_sipush) {
					ret = readUnsignedShort(bytecodes, u + 1);
				} else if (opcode == JVM_OPC_bipush) {
					ret = bytecodes[u+1] & 0xff;
				} else if (opcode == JVM_OPC_iconst_0)
					ret = 0;
				else if (opcode == JVM_OPC_iconst_1)
					ret = 1;
				else if (opcode == JVM_OPC_iconst_2)
					ret = 2;
				else if (opcode == JVM_OPC_iconst_3)
					ret = 3;
				else if (opcode == JVM_OPC_iconst_4)
					ret = 4;
				else if (opcode == JVM_OPC_iconst_5)
					ret = 5;
			}
			u += len;
		}
	}
//	printf("Location %s %s bci %d -> %d\n", methodName, methodDesc,
//			frames[depth].location, ret);

	jvmti->Deallocate(bytecodes);
//	jvmti->Deallocate(methodName);
//	jvmti->Deallocate(methodDesc);

	return ret;
}

/* Callback for JVMTI_EVENT_VM_START */
static void JNICALL
cbVMStart(jvmtiEnv *jvmti, JNIEnv *env) {

	enter_critical_section(jvmti);
	{
		jclass klass;
		jfieldID field;
		jint rc;
		jvmtiJlocationFormat fmt;

		/* Java Native Methods for class */
		static JNINativeMethod registry[1] = { { "_getTraceLocation", "(I)I",
				(void*) &getTraceLocation } };
		/* Register Natives for class whose methods we use */
		klass = env->FindClass(
				"de/unisb/cs/st/javaslicer/tracer/LazyTraceLocator");
		if (klass == NULL) {
			fatal_error(
					"ERROR: JNI: Cannot find de.unisb.cs.st.javaslicer.tracer.LazyTraceLocator with FindClass\n");
		}
		rc = env->RegisterNatives(klass, registry, 1);
		if (rc != 0) {
			fatal_error(
					"ERROR: JNI: Cannot register natives for class de.unisb.cs.st.javaslicer.tracer.LazyTraceLocator\n");
		}
		/* Engage calls. */
		field = env->GetStaticFieldID(klass, "engaged", "I");
		if (field == NULL) {
			fatal_error("ERROR: JNI: Cannot get field from %s\n",
					STRING(HEAP_TRACKER_class));
		}
		env->SetStaticIntField(klass, field, 1);

		/* Indicate VM has started */
		gdata->vm_is_started = JNI_TRUE;
		jvmti->GetJLocationFormat(&fmt);
		if (fmt != 1) {
			fatal_error(
					"Error: For some reason, your JVM is not providing bytecode indices for stack traces");
		}
	}
	exit_critical_section(jvmti);
}
// VM Death callback
static void JNICALL callbackVMDeath(jvmtiEnv *jvmti_env, JNIEnv* jni_env) {
	gdata->vmDead = JNI_TRUE;
}

static void JNICALL callbackVMInit(jvmtiEnv * jvmti, JNIEnv * env,
		jthread thread) {
	jvmtiError err;
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options,
		void *reserved) {
	static GlobalAgentData data;
	jvmtiError error;
	jint res;
	jvmtiEventCallbacks callbacks;

	/* Setup initial global agent data area
	 *   Use of static/extern data should be handled carefully here.
	 *   We need to make sure that we are able to cleanup after ourselves
	 *     so anything allocated in this library needs to be freed in
	 *     the Agent_OnUnload() function.
	 */
	(void) memset((void*) &data, 0, sizeof(data));
	gdata = &data;

	/*  We need to first get the jvmtiEnv* or JVMTI environment */

	gdata->jvm = jvm;
	res = jvm->GetEnv((void **) &jvmti, JVMTI_VERSION_1_0);

	if (res != JNI_OK || jvmti == NULL) {
		/* This means that the VM was unable to obtain this version of the
		 *   JVMTI interface, this is a fatal error.
		 */
		printf("ERROR: Unable to access JVMTI Version 1 (0x%x),"
				" is your J2SE a 1.5 or newer version?"
				" JNIEnv's GetEnv() returned %d\n", JVMTI_VERSION_1, res);

	}

	/* Here we save the jvmtiEnv* for Agent_OnUnload(). */
	gdata->jvmti = jvmti;
	(void) memset(&capa, 0, sizeof(jvmtiCapabilities));
	capa.can_get_line_numbers = 1;
	capa.can_get_bytecodes = 1;
	capa.can_get_constant_pool = 1;

	error = jvmti->AddCapabilities(&capa);
	check_jvmti_error(jvmti, error,
			"Unable to get necessary JVMTI capabilities.");

	(void) memset(&callbacks, 0, sizeof(callbacks));
	callbacks.VMInit = &callbackVMInit; /* JVMTI_EVENT_VM_INIT */
	callbacks.VMDeath = &callbackVMDeath; /* JVMTI_EVENT_VM_DEATH */
	callbacks.VMStart = &cbVMStart;


	error = jvmti->SetEventCallbacks(&callbacks, (jint) sizeof(callbacks));
	check_jvmti_error(jvmti, error, "Cannot set jvmti callbacks");

	error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_START,
			(jthread) NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");
	error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT,
			(jthread) NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");
	error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH,
			(jthread) NULL);
	check_jvmti_error(jvmti, error, "Cannot set event notification");

	/* Here we create a raw monitor for our use in this agent to
	 *   protect critical sections of code.
	 */
	error = jvmti->CreateRawMonitor("agent data", &(gdata->lock));
	check_jvmti_error(jvmti, error, "Cannot create raw monitor");

	/* We return JNI_OK to signify success */
	return JNI_OK;
}
