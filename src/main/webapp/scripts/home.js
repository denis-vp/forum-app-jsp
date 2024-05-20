const postFactory = (id, username, title, content, ownerId) => {
    const storedUserId = JSON.parse(localStorage.getItem('user')).id;
    let deleteButton = '';
    if (ownerId === storedUserId) {
        deleteButton = `<a id="deletePostButton${id}" href="#" class="card-link">Delete</a>`;
    }
    return `
    <div id="post${id}" class="card" style="width: 18rem;">
        <div class="card-body">
            <h5 class="card-title">${title}</h5>
            <h6 class="card-subtitle mb-2 text-muted">${username}</h6>
            <p class="card-text">${content}</p>
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

window.onload = () => {
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