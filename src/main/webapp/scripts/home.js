const postFactory = (id, username, title, content, ownerId) => {
    const storedUserId = JSON.parse(localStorage.getItem('user')).id;
    let deleteButton = '';
    if (ownerId === storedUserId) {
        deleteButton = `<a id="deletePostButton${id}" href="#" class="card-link">Delete</a>`;
    }
    return `
    <div id="post${id}" class="card mb-3">
        <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
                <div class="h5 m-0">@${username}</div>
            </div>
        </div>
        <div class="card-body">
            <h4 class="card-title">${title}</h4>
            <p class="card-text">${content}</p>
        </div>
        <div class="card-footer">
            <a href="./comments.jsp?postId=${id}" class="card-link">Comments</a>
            ${deleteButton}
        </div>
    </div>`;
}

const setUpPost = (id) => {
    $(`#deletePostButton${id}`).click((event) => {
        event.preventDefault();

        if (!confirm('Are you sure you want to delete this post?')) {
            return;
        }

        $.ajax({
            url: '/forum_app_jsp_war_exploded/post/' + id,
            method: 'DELETE',
            contentType: 'application/json',
            headers: {
                'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
            },
            success: (data) => {
                $(`#post${id}`).remove();
            },
            error: (jqXHR, textStatus, errorThrown) => {
                if (jqXHR.status === 403) {
                    alert('You are not allowed to delete this post');
                } else {
                    let win = window.open('', '_self');
                    win.document.write(jqXHR.responseText);
                }
            }
        });
    });
}

const getNewPost = (id, posts) => {
    $.ajax({
        url: '/forum_app_jsp_war_exploded/post/' + id,
        method: 'GET',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
        },
        success: (post) => {
            posts.append(postFactory(post.id, post.user.username, post.title, post.content, post.user.id));
            setUpPost(post.id);
        },
        error: (jqXHR, textStatus, errorThrown) => {
            let win = window.open('', '_self');
            win.document.write(jqXHR.responseText);
        }
    });
}

window.onload = () => {
    $('#newPostButton').click(() => {
        const title = $('#postTitle').val();
        const content = $('#postContent').val();

        if (!title || !content) {
            alert('Please fill in all fields');
            return;
        }

        $.ajax({
            url: '/forum_app_jsp_war_exploded/post/',
            method: 'POST',
            data: JSON.stringify({
                title,
                content
            }),
            contentType: 'application/json',
            headers: {
                'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
            },
            success: (data) => {
                getNewPost(data, posts);
                $('#postTitle').val('');
                $('#postContent').val('');
            },
            error: (jqXHR, textStatus, errorThrown) => {
                let win = window.open('', '_self');
                win.document.write(jqXHR.responseText);
            }
        });
    });

    $('#postCreateButton').click(() => {
        window.location.href = './postCreate.jsp';
    });

    const posts = $('#postsList');
    $.ajax({
        url: '/forum_app_jsp_war_exploded/post/',
        method: 'GET',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + JSON.parse(localStorage.getItem('token')),
        },
        success: (data) => {
            data.forEach(post => {
                posts.append(postFactory(post.id, post.user.username, post.title, post.content, post.user.id));
                setUpPost(post.id);
            });
        },
        error: (jqXHR, textStatus, errorThrown) => {
            let win = window.open('', '_self');
            win.document.write(jqXHR.responseText);
        }
    });
}