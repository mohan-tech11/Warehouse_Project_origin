// ================= HERO BUTTONS =================

function getStarted() {

    document
        .getElementById("products")
        .scrollIntoView({
            behavior: "smooth"
        });

}


function learnMore() {

    document
        .querySelector(".about")
        .scrollIntoView({
            behavior: "smooth"
        });

}


// ================= LOGIN =================

function login() {

    window.location.href = "login.html";

}


// ================= SEARCH =================

function searchWebsite() {

    let search = prompt("What do you want to search?");

    if (search) {

        alert("Searching for: " + search);

    }

}


// ================= SERVICE BUTTONS =================

function openProducts() {

    window.location.href = "products.html";

}


function openInventory() {

    window.location.href = "inventory.html";

}


function openOrders() {

    window.location.href = "orders.html";

}


function openCustomers() {

    window.location.href = "customers.html";

}


function openWarehouses() {

    window.location.href = "warehouses.html";

}


function openDispatches() {

    window.location.href = "dispatch.html";

}


// ================= HERO SLIDER =================

let currentSlide = 0;

const heroImages = [

    "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=1800&q=85",

    "https://images.unsplash.com/photo-1553413077-190dd305871c?auto=format&fit=crop&w=1800&q=85",

    "https://images.unsplash.com/photo-1587293852726-70cdb56c2866?auto=format&fit=crop&w=1800&q=85"

];


function changeHeroImage() {

    const hero = document.querySelector(".hero");

    hero.style.backgroundImage =
        `url("${heroImages[currentSlide]}")`;

}


function nextImage() {

    currentSlide++;

    if (currentSlide >= heroImages.length) {

        currentSlide = 0;

    }

    changeHeroImage();

}


function previousImage() {

    currentSlide--;

    if (currentSlide < 0) {

        currentSlide = heroImages.length - 1;

    }

    changeHeroImage();

}
// ================= DASHBOARD STATISTICS =================

async function loadDashboardStats() {
try {
const response = await fetch("dashboardStats");

    if (!response.ok) {
        throw new Error("Unable to load dashboard statistics");
    }

    const stats = await response.json();

    document.getElementById("totalProducts").textContent = stats.products;
    document.getElementById("totalCustomers").textContent = stats.customers;
    document.getElementById("totalOrders").textContent = stats.orders;
    document.getElementById("totalWarehouses").textContent = stats.warehouses;
    document.getElementById("totalDispatches").textContent = stats.dispatches;

} catch (error) {
    console.error("Dashboard statistics error:", error);

    document.querySelectorAll(".stats-container .stat-card p")
        .forEach(element => {
            element.textContent = "Unavailable";
        });
}


}

document.addEventListener("DOMContentLoaded", loadDashboardStats);
