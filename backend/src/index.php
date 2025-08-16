<?php

$MYSQL_HOST = getenv('MYSQL_HOST');
$MYSQL_DATABASE = getenv('MYSQL_DATABASE');
$MYSQL_USER = getenv('MYSQL_USER');
$MYSQL_PASSWORD = getenv('MYSQL_PASSWORD');

$text = "<h1>hello</h1>";
$text .= "MYSQL_DATABASE: $MYSQL_DATABASE<br>";
$text .= "MYSQL_USER: $MYSQL_USER<br>";
$text .= "MYSQL_PASSWORD: $MYSQL_PASSWORD<br>";

$dsn = "mysql:host=$MYSQL_HOST;dbname=$MYSQL_DATABASE;charset=utf8mb4";
$options = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES => false,
];

print_r($text);

$jsonString = file_get_contents("../config/config.json");

$config = json_decode($jsonString, true);

echo "<hr>";
echo "<h2>Config Data</h2>";
print_r($config);
echo "<hr>";

// 链接到数据库
try {
    $pdo = new PDO($dsn, $MYSQL_USER, $MYSQL_PASSWORD, $options);
    echo "<p>数据库连接成功</p>";

    // 获取所有数据库列表
    $stmt = $pdo->query("SHOW DATABASES");
    // while ($row = $stmt->fetch()) {
    //     $list[] = $row[0];
    // }
    echo "<h2>Databases</h2>";
    print_r($stmt->fetchAll(PDO::FETCH_COLUMN));
} catch (PDOException $e) {
    echo "数据库连接失败: " . $e->getMessage();
    exit;
}
