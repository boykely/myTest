<?php
header("Content-Type:text/html");
$homepage = file_get_contents('http://www.banque-centrale.mg/');
echo $homepage;
?>