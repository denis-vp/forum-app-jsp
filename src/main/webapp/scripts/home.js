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

window.onload(() => {
    const posts = $('#postsList');
    $.ajax({
        url: '/post/',
        type: 'GET',
        dataType: 'json',
        success: (data) => {
            data.forEach(post => {
                posts.append(postFactory(post.user.username, post.title, post.content));
            });
        },
        error: (error) => {
            console.log(error);
        }
    });
})