const commentFactory = (id, username, content, ownerId) => {
    const storedUserId = JSON.parse(localStorage.getItem('user')).id;
    let deleteButton = '';
    if (ownerId === storedUserId) {
        deleteButton = `<a id="deleteCommentButton${id}" href="#">Delete</a>`;
    }

    return `
    <div id="comment${id}" class="card mb-3">
        <div class="card-body">
            <h6 class="text-primary fw-bold mb-3">@${username}</h6>
            <p class="card-text mb-2">${content}</p>
            ${deleteButton}
        </div>
    </div>`;
}

const setUpComment = (id) => {
    $(`#deleteCommentButton${id}`).click((event) => {
        event.preventDefault();

        if (!confirm('Are you sure you want to delete this comment?')) {
            return;
        }

        $.ajax({
            url: '/forum_app_jsp_war_exploded/comment/' + id,
            method: 'DELETE',
            contentType: 'application/json',
            headers: {
                'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
            },
            success: (data) => {
                $(`#comment${id}`).remove();
            },
            error: (jqXHR, textStatus, errorThrown) => {
                if (jqXHR.status === 403) {
                    alert('You are not allowed to delete this comment');
                } else {
                    let win = window.open('', '_self');
                    win.document.write(jqXHR.responseText);
                }
            }
        });
    });
}

const getNewComment = (id, comments) => {
    $.ajax({
        url: '/forum_app_jsp_war_exploded/comment/' + id,
        method: 'GET',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
        },
        success: (comment) => {
            comments.append(commentFactory(comment.id, comment.user.username, comment.content, comment.user.id));
            setUpComment(comment.id);
        },
        error: (jqXHR, textStatus, errorThrown) => {
            let win = window.open('', '_self');
            win.document.write(jqXHR.responseText);
        }
    });
}

window.onload = () => {
    const url = window.location.href;
    const id = url.substring(url.lastIndexOf('=') + 1);
    $.ajax({
        url: '/forum_app_jsp_war_exploded/post/' + id,
        method: 'GET',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
        },
        success: (data) => {
            $('#postTitle').text(data.title);
            $('#postUsername').text("@" + data.user.username);
            $('#postContent').text(data.content);
        },
        error: (jqXHR, textStatus, errorThrown) => {
            let win = window.open('', '_self');
            win.document.write(jqXHR.responseText);
        }
    });

    const comments = $('#commentsList');
    $.ajax({
        url: '/forum_app_jsp_war_exploded/comment/post/' + id,
        method: 'GET',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
        },
        success: (data) => {
            data.forEach(comment => {
                comments.append(commentFactory(comment.id, comment.user.username, comment.content, comment.user.id));
                setUpComment(comment.id);
            });
        },
        error: (jqXHR, textStatus, errorThrown) => {
            let win = window.open('', '_self');
            win.document.write(jqXHR.responseText);
        }
    });

    $('#newComment').keypress((event) => {
        if (event.which !== 13) {
            return;
        }

        event.preventDefault();

        const content = $('#newComment').val();
        if (content === '') {
            alert('Comment cannot be empty');
            return;
        }

        $.ajax({
            url: '/forum_app_jsp_war_exploded/comment/?postId=' + id,
            method: 'POST',
            data: JSON.stringify({
                content: content,
            }),
            contentType: 'application/json',
            headers: {
                'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
            },
            success: (data) => {
                getNewComment(data, comments);
                $('#newComment').val('');
            },
            error: (jqXHR, textStatus, errorThrown) => {
                let win = window.open('', '_self');
                win.document.write(jqXHR.responseText);
            }
        })
    });
}