#!/usr/bin/perl -w

use warnings;
use strict;

sub addHeader {
	my $filename = shift;
	my $newfilename = $filename."_subst_header";

	open OLD, $filename || die "Cannot open file $filename: $!\n";
	open NEW, ">", $newfilename || die "Cannot open file $newfilename: $!\n";

	my ($component, $package, $class) = ("", "", "");
	if ($filename =~ /^(.+)\/src\/(?:main|test)\/java\/(.+)\/([^\/]+)\.java/) {
		($component, $package, $class) = ($1, $2, $3);
		$package =~ tr$/$.$;
	} else {
		print "Could not extract component, package and class name from filename $filename\n";
	}

	print NEW <<HEADER_END;
/** License information:
 *    Component: $component
 *    Package:   $package
 *    Class:     $class
 *    Filename:  $filename
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
HEADER_END
	
	my $line = <OLD>;
	if ($line =~ /^\s*\/\*\*.*$/) {
		while ($line = <OLD>) {
			last if ($line =~ /\*\//);
		}
	} else {
		print NEW $line;
	}

	print NEW <OLD>;

	close NEW || die "Cannot clone $newfilename: $!\n";
	close OLD || die "Cannot clone $filename: $!\n";

	if (system("cmp", "-s", $newfilename, $filename) != 0) {
		print "Generated difference in $filename\n";
		rename($newfilename, $filename);
	} else {
		unlink($newfilename);
	}
}

sub processDir {
	my $dirname = shift;
	$dirname =~ s/\/+$//;
	opendir(my $DIR, $dirname) || die "Cannot open dir $dirname: $!\n";
	while (my $file = readdir($DIR)) {
		next if ($file eq ".") or ($file eq "..");
		my $full = $dirname eq "." ? $file : $dirname."/".$file;
		if (-d $full) {
			processDir($full);
		} elsif (-f $full) {
			addHeader($full) if ($file =~ /\.java$/);
		} else {
			print "Unknown file: $full\n";
		}
	}
	closedir($DIR) || die "Cannot close dir $dirname: $!\n";
}

if ($#ARGV eq -1) {
	processDir(".");
} else {
	foreach my $dir (@ARGV) {
		processDir($dir);
	}
}


