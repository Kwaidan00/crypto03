#!/bin/bash
tcpdump -i wlp3s0 -A -l -vv host www.zak.pwr.wroc.pl -c 256 > test
grep -E "^Cookie: wordpress" test | tr ';' '\n'
