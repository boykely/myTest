<?php
header("Content-Type:text/html");
$url=$_GET['url'];
$homepage = file_get_contents($url);
echo $homepage;
?>