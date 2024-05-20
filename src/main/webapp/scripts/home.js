const postFactory = (username, title, content) => {
    return `
    <div class="card" style="width: 18rem;">
        <div class="card-body">
            <h5 class="card-title">${title}</h5>
            <h6 class="card-subtitle mb-2 text-muted">${username}</h6>
            <p class="card-text">${content}</p>
            <a href="#" class="card-link">Comments</a>
        </div>
    </div>`;
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
                posts.append(postFactory(post.user.username, post.title, post.content));
            });
        },
        error: (jqXHR, textStatus, errorThrown) => {
            let win = window.open('', '_self');
            win.document.write(jqXHR.responseText);
        }
    });
}