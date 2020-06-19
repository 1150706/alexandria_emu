<?php echo '<?xml version="1.0" encoding="utf-8"?>' ?>
<rss version="2.0">
	<channel>
		<title>Berlyz Online</title>
		<link>http://berlyz.com/</link>
		<description>MultiServer</description>
<?php
	require "config.php";
	mysql_connect(DB_HOST,DB_LOGIN,DB_PASS);
	mysql_select_db(DB_BDD);
	mysql_query("SET NAMES UTF8"); 
	
	$sql="SELECT * FROM client_rss_news ORDER BY id DESC LIMIT 0, 6";
	$req = mysql_query($sql) or die('Erreur SQL !<br>'.$sql.'<br />'.mysql_error());
	while($data=mysql_fetch_assoc($req)){
		echo "<item>\n";
		echo "<guid>{$data["id"]}</guid>";
		echo "<title>{$data["titulo"]}</title>\n";
		echo "<link>{$data["link"]}</link>\n";
		echo "<icon>{$data["icono"]}</icon>\n";
		echo "<pubDate>".date("D, d M Y H:i:s",strtotime($data["date"]))." +0200</pubDate>\n";
		echo "</item>\n";
	}
?>
	</channel>
</rss>