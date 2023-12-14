<#ftl encoding="UTF-8">
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Welcome to TaskManager Application, ${name}!</title>
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
        <h1 style="text-align: center">Welcome to TaskManager Application</h1>
        <p>Hello, ${name}!</p>
        <p>This email confirms your successful registration.
            You can now log in and start managing your tasks.</p>
        <p>Best regards, <br>Denys Soldatenko</p>
    </div>
</div>
</body>
</html>
