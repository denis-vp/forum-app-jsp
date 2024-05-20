<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Post Preview</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <script src="../scripts/comments.js"></script>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="col-md-8">
            <div class="post-content">
                <div class="post-container">
                    <div class="post-title">
                        <h1 id="postTitle"></h1>
                    </div>
                    <div id="postDetails" class="post-detail">
                        <div class="user-info">
                            <p id="postUsername"></p>
                        </div>
                        <div class="line-divider"></div>
                        <div class="post-text">
                            <p id="postContent"></p>
                        </div>
                        <div class="line-divider"></div>
                    </div>
                    <input id="newComment" type="text" name="new-comment" />
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
