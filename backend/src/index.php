<?php

$MYSQL_ROOT_PASSWORD = getenv('MYSQL_ROOT_PASSWORD');
$MYSQL_DATABASE = getenv('MYSQL_DATABASE');
$MYSQL_USER = getenv('MYSQL_USER');
$MYSQL_PASSWORD = getenv('MYSQL_PASSWORD');

$text = "<h1>hello</h1>";
$text .= "MYSQL_ROOT_PASSWORD: $MYSQL_ROOT_PASSWORD<br>";
$text .= "MYSQL_DATABASE: $MYSQL_DATABASE<br>";
$text .= "MYSQL_USER: $MYSQL_USER<br>";
$text .= "MYSQL_PASSWORD: $MYSQL_PASSWORD<br>";

print_r($text);

$jsonString = file_get_contents("../config/config.json");

$config = json_decode($jsonString, true);

echo "<hr>";
echo "<h2>Config Data</h2>";
print_r($config);
