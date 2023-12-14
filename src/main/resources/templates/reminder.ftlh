<#ftl encoding="UTF-8">
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Reminder: Task due in 1 hour</title>
    <style>
        #body {
            display: flex;
            flex-direction: column;
            font-family: "Segoe UI", serif;
            align-items: center;
            background-color: #607FF2;
            font-size: 20px;
            height: 100%;
        }

        #div {
            display: block;
            flex-direction: column;
            margin: 10%;
            font-family: "Segoe UI", serif;
            background-color: white;
            padding: 3%;
            border-radius: 20px;
            color: black;
        }
    </style>
</head>
<body>
<div id="body">
    <div id="div">
        <h1 style="text-align: center">Reminder</h1>
        <p>Hello, ${name}!</p>
        <p>This is a friendly reminder that you have a task due in 1 hour.</p>
        <div>
            <hr>
                <p>Title: ${title}</p>
                <p>Description: ${description}</p>
            <hr>
        </div>
        <p>You can find more information about this task in the app.</p>
        <p>Best regards, <br>Denys Soldatenko</p>
    </div>
</div>
</body>
</html>
