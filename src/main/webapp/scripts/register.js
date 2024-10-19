window.onload = () => {
    $('#registerButton').click(() => {
        const username = $('#usernameInput').val();
        const email = $('#emailInput').val();
        const password = $('#passwordInput').val();
        const repeatPassword = $('#repeatPasswordInput').val();

        if (!username || !email || !password || !repeatPassword) {
            alert('All fields are required');
            return;
        } else if (password !== repeatPassword) {
            alert('Passwords do not match');
            return;
        }

        $.ajax({
            url: '/forum_app_jsp_war_exploded/user/register',
            method: 'POST',
            data: JSON.stringify({
                username,
                email,
                password
            }),
            contentType: 'application/json',
            success: () => {
                window.location.href = './login.jsp';
            },
            error: (jqXHR, textStatus, errorThrown) => {
                let win = window.open('', '_self');
                win.document.write(jqXHR.responseText);
            }
        });
    });
}