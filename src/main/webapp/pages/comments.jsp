<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Post Preview</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <script src="../scripts/comments.js"></script>
</head>
<body style="background-color: #eee;">
<div class="container">
    <div class="row justify-content-center mt-3">
        <div class="col-md-10">
            <div class="card mb-3">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <div id="postUsername" class="h5 m-0"></div>
                    </div>
                </div>
                <div class="card-body">
                    <h4 id="postTitle" class="card-title"></h4>
                    <p id="postContent" class="card-text"></p>
                </div>
                <div class="card-footer">
                    <input id="newComment" class="form-control" type="text" name="new-comment"
                           placeholder="Write a new comment"/>
                </div>
            </div>
        </div>
        <div id="commentsList" class="col-md-9 offset-md-1">
        </div>
    </div>
    </div>
</div>
</body>
</html>
