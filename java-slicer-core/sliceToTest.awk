{
  method=line=instr=$0
  if (sub(":.*", "", method) == 0 ||
      sub(".*:", "", line) == 0 || sub(" .*", "", line) == 0 ||
      sub("[^ ]* ", "", instr) == 0) {
    printf "Illegal line: %s\n", $0 >"/dev/stderr"
  } else {
    result = sprintf("%s  new String[] { \"%s\", \"%s\", \"%s\" },\n", result, method, line, instr);
  }

}

END {
  printf "new String[][] {\n%s}\n", result;
}

