window.onload = () => {
    $('#postCreateButton').click(() => {
        const title = $('#titleInput').val();
        const content = $('#contentInput').val();

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
                window.location.href = './home.jsp';
            },
            error: (jqXHR, textStatus, errorThrown) => {
                let win = window.open('', '_self');
                win.document.write(jqXHR.responseText);
            }
        });
    });
}