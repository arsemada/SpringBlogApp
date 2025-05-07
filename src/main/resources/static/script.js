const API_BASE_URL = 'http://localhost:8080/api';
const AUTH_API_BASE_URL = 'http://localhost:8080/auth';
const messageBox = document.getElementById('message-box');
const loginRegisterContainer = document.getElementById('login-register-container');
const blogContainer = document.getElementById('blog-container');
const blogPostsContainer = document.getElementById('blog-posts');
const newBlogForm = document.getElementById('new-blog-form');

let currentUser = null; // Store logged-in user info

function showMessage(message, type = 'success') {
    messageBox.textContent = message;
    messageBox.className = `fixed top-4 left-1/2 transform -translate-x-1/2 bg-${type === 'success' ? 'green' : 'red'}-100 text-${type === 'success' ? 'green' : 'red'}-700 border border-${type === 'success' ? 'green' : 'red'}-400 px-4 py-2 rounded shadow-md`;
    messageBox.classList.add('show');
    setTimeout(() => {
        messageBox.classList.remove('show');
    }, 3000);
}

function validateForm(formId) {
    const form = document.getElementById(formId);
    let isValid = true;
    const inputs = form.querySelectorAll('input, textarea');
    inputs.forEach(input => {
        const errorId = `${input.id}-error`;
        const errorElement = document.getElementById(errorId);
        if (!input.value.trim()) {
            errorElement.textContent = `${input.name.charAt(0).toUpperCase() + input.name.slice(1)} is required`;
            errorElement.style.display = 'block';
            isValid = false;
        } else {
            errorElement.style.display = 'none';
        }
    });
    return isValid;
}

async function registerUser(event) {
    event.preventDefault();
    if (!validateForm('register-form')) {
        return;
    }

    const usernameInput = document.getElementById('register-username');
    const passwordInput = document.getElementById('register-password');
    const username = usernameInput.value;
    const password = passwordInput.value;

    try {
        const response = await fetch(`${AUTH_API_BASE_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Registration failed');
        }
        showMessage('Registration successful! Please login.', 'success');
        document.getElementById('switch-to-login').click();
        clearForm('register-form');

    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

async function loginUser(event) {
    event.preventDefault();
    if (!validateForm('login-form')) {
        return;
    }

    const usernameInput = document.getElementById('login-username');
    const passwordInput = document.getElementById('login-password');
    const username = usernameInput.value;
    const password = passwordInput.value;

    try {
        const response = await fetch(`${AUTH_API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Login failed');
        }
        const data = await response.json();
        localStorage.setItem('user', JSON.stringify({ username: username, token: data.token }));
        showMessage('Login successful!', 'success');
        currentUser = { username, token: data.token };
        showBlogPage();

    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

function clearForm(formId) {
    const form = document.getElementById(formId);
    const inputs = form.querySelectorAll('input, textarea');
    inputs.forEach(input => {
        input.value = '';
        const errorId = `${input.id}-error`;
        const errorElement = document.getElementById(errorId);
        if (errorElement) {
            errorElement.style.display = 'none';
        }
    });
}

document.getElementById('switch-to-register').addEventListener('click', () => {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'block';
});

document.getElementById('switch-to-login').addEventListener('click', () => {
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('register-form').style.display = 'none';
});

document.getElementById('login-button').addEventListener('click', loginUser);
document.getElementById('register-button').addEventListener('click', registerUser);

function showBlogPage() {
    loginRegisterContainer.style.display = 'none';
    blogContainer.style.display = 'block';
    if (currentUser) {
        newBlogForm.style.display = 'block';
    } else {
        newBlogForm.style.display = 'none';
    }
    loadBlogPosts();
}



async function createBlogPost(event) {
    event.preventDefault();
    if (!validateForm('new-blog-form')) {
        return;
    }

    const titleInput = document.getElementById('blog-title');
    const contentInput = document.getElementById('blog-content');
    const title = titleInput.value;
    const content = contentInput.value;

    try {
        const response = await fetch(`${API_BASE_URL}/blogs`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentUser.token}`
            },
            body: JSON.stringify({ title, content })
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to create blog post');
        }
        showMessage('Blog post created successfully!', 'success');
        clearForm('new-blog-form');
        loadBlogPosts();
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

async function loadBlogPosts() {
    blogPostsContainer.innerHTML = '';
    try {
        const response = await fetch(`${API_BASE_URL}/blogs`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentUser.token}`
            }
        });
        if (!response.ok) {
            throw new Error('Failed to load blog posts');
        }
        const posts = await response.json();

        posts.forEach(post => {
            const postElement = document.createElement('div');
            postElement.classList.add('blog-card');
            postElement.innerHTML = `
                    <h2 class="text-xl font-semibold text-gray-800 mb-2">${post.title}</h2>
                    <p class="text-gray-600 mb-2">By: <span class="font-semibold text-orange-500">${post.author}</span></p>
                    <p class="text-gray-700 mb-4">${post.content}</p>
                    <p class="text-gray-500 text-sm">Created At: ${new Date(post.createdAt).toLocaleString()}</p>
                `;
            blogPostsContainer.appendChild(postElement);
        });
    } catch (error) {
        showMessage(`Error: ${error.message}`, 'error');
    }
}

document.getElementById('create-blog-button').addEventListener('click', createBlogPost);

// Logout functionality
document.getElementById('logout-button').addEventListener('click', () => {
    localStorage.removeItem('user'); // Clear user data
    currentUser = null;
    blogPostsContainer.innerHTML = ''; // Clear blog posts
    blogContainer.style.display = 'none'; // Hide blog container
    loginRegisterContainer.style.display = 'block'; // Show login/register
    showMessage('Logged out successfully!', 'success');
});

// Check for existing login on page load
const storedUser = localStorage.getItem('user');
if (storedUser) {
    currentUser = JSON.parse(storedUser);
    showBlogPage();
}
