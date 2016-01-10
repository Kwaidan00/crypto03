#!/bin/bash
tcpdump -i wlp3s0 -A -l -vv host www.zak.pwr.wroc.pl -c 256 > test
grep -E "^Cookie: wordpress" test | awk '{split($0,a,";"); split(a[1],b,":");print "Po lewej stronie nazwa ciasteczka, po prawej stronie zawartość ciasteczka\n\n",b[2],"\n\n",a[2],"\n\n",a[7],"\n\nTo wszystkie niezbędne ciasteczka. Dodaj je do przeglądarki za pomocą np Cookies Manager+ (dla FF).\n"}'
# Plik z ciasteczkami znajduje się (pod Linuxem) w katalogu ~/.mozilla/firefox/<nazwa_profilu>/cookies.sqlite. W ten sposób należałoby dodać te ciasteczka automatycznie.
# Dla strony www.zak.pwr.wroc.pl wystarczają te ciasteczka.  
