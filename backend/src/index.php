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
    echo "<h2>Databases</h2>";
    print_r($stmt->fetchAll(PDO::FETCH_COLUMN));
} catch (PDOException $e) {
    echo "数据库连接失败: " . $e->getMessage();
    exit;
}

echo "<hr>";

$data = ["value" => 123];
$options = [
    "http" => [
        "header"  => "Content-Type: application/json\r\n",
        "method"  => "POST",
        "content" => json_encode($data),
    ],
];
$context  = stream_context_create($options);
$response = file_get_contents("http://host.docker.internal:5000/predict", false, $context);

// 修复：file_get_contents 返回的是字符串，需要先 json_decode
$responseData = json_decode($response, true);

echo "Python 返回: " . ($responseData["msg"] ?? "无返回消息");
echo "<br>";
echo "数据库返回: " . ($responseData["database"] ?? "无返回消息");

echo "<hr>";

// 如果数据库返回的是二维数组，则以表格形式展示
if (isset($responseData["database"]) && is_array($responseData["database"]) && count($responseData["database"]) > 0 && is_array($responseData["database"][0])) {
    echo '<br><h3>数据库表格展示</h3>';
    echo '<table border="1" cellpadding="5" style="border-collapse:collapse;">';
    // 表头
    echo '<tr>';
    foreach (array_keys($responseData["database"][0]) as $col) {
        echo '<th>' . htmlspecialchars($col ?? '', ENT_QUOTES, 'UTF-8') . '</th>';
    }
    echo '</tr>';
    // 数据行
    foreach ($responseData["database"] as $row) {
        echo '<tr>';
        foreach ($row as $cell) {
            echo '<td>' . htmlspecialchars($cell ?? '', ENT_QUOTES, 'UTF-8') . '</td>';
        }
        echo '</tr>';
    }
    echo '</table>';
}
