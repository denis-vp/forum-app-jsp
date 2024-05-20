<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Post Creation</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="./../scripts/postCreate.js"></script>
</head>
<body>
    <h1>Post Creation</h1>
    <form>
        <table>
            <tr>
                <td>Title:</td>
                <td><input id="titleInput" type="text" name="title" /></td>
            </tr>
            <tr>
                <td>Content:</td>
                <td><textarea id="contentInput" name="content" rows="5" cols="40"></textarea></td>
            </tr>
            <tr>
                <td colspan="2"><button id="postCreateButton" type="button">Create</button></td>
            </tr>
        </table>
    </form>
    <a href="./home.jsp">Back to List</a>
</body>
</html>
