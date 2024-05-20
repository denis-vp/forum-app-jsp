window.onload = () => {
    $('#loginButton').click(() => {
        const email = $('#emailInput').val();
        const password = $('#passwordInput').val();

        if (!email || !password) {
            alert('Please fill in all fields');
            return;
        }

        $.ajax({
            url: '/forum_app_jsp_war_exploded/user/login',
            method: 'POST',
            data: JSON.stringify({
                email,
                password
            }),
            contentType: 'application/json',
            success: (data) => {
                localStorage.setItem('user', JSON.stringify(data.user));
                localStorage.setItem('token', JSON.stringify(data.token));
                window.location.href = './home.jsp';
            },
            error: (jqXHR, textStatus, errorThrown) => {
                let win = window.open('', '_self');
                win.document.write(jqXHR.responseText);
            }
        });
    });
}