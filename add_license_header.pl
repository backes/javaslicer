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
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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


