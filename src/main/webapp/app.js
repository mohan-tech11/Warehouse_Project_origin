function showForm(formId) {
    document.getElementById(formId).classList.remove("hidden");
}

function hideForm(formId) {
    document.getElementById(formId).classList.add("hidden");
}


function loadProducts() {

    fetch("addProduct")
        .then(response => response.json())
        .then(products => {

            const table = document.getElementById("productTable");

            table.innerHTML = "";

            if (products.length === 0) {

                table.innerHTML = `
                    <tr>
                        <td colspan="7">
                            No products found.
                        </td>
                    </tr>
                `;

                return;
            }

            products.forEach(product => {

                const row = `
                    <tr>

                        <td>${product.id}</td>

                        <td>${product.name}</td>

                        <td>${product.sku}</td>
                        <td>${product.category}</td>

                        <td>₹${product.price}</td>

                        <td>${product.quantity}</td>

                        <td>
                            <button type="button" onclick="editProduct(${product.id})">
                                Edit
                            </button>

                            <button type="button" onclick="deleteProduct(${product.id})">
                                Delete
                            </button>
                        </td>

                    </tr>
                `;

                table.innerHTML += row;

            });

        })
        .catch(error => {

            console.error("Error loading products:", error);

            document.getElementById("productTable").innerHTML = `
                <tr>
                    <td colspan="7">
                        Error loading products.
                    </td>
                </tr>
            `;
        });
}
let allOrders = [];

function loadOrders() {

    fetch("addOrder")
        .then(response => response.json())
        .then(orders => {

            const table = document.getElementById("orderTable");
            if (!table) return;
            if (!Array.isArray(orders)) {
                throw new Error("Orders response is not a list.");
            }

            allOrders = orders;
            filterOrders(
                document.getElementById("orderSearch")?.value || ""
            );
        })
        .catch(error => {
            console.error("Error loading orders:", error);
            const table = document.getElementById("orderTable");
            if (table) {
                table.innerHTML = `
                    <tr>
                        <td colspan="5">Error loading orders.</td>
                    </tr>
                `;
            }
        });
}

function filterOrders(searchText) {
    const query = String(searchText || "").trim().toLocaleLowerCase();
    const filteredOrders = allOrders.filter(order =>
        [
            order.id,
            order.customerName || "Customer #" + order.customerId,
            order.amount,
            Number(order.amount).toLocaleString("en-IN"),
            "₹" + Number(order.amount).toLocaleString("en-IN"),
            order.status
        ].some(value =>
            String(value == null ? "" : value)
                .toLocaleLowerCase()
                .includes(query)
        )
    );

    displayOrders(filteredOrders);
}

function displayOrders(orders) {
    const table = document.getElementById("orderTable");
    if (!table) return;
    table.innerHTML = "";

    if (orders.length === 0) {
        table.innerHTML = `
            <tr>
                <td colspan="5">${
                    allOrders.length === 0
                        ? "No orders found."
                        : "No orders match your search."
                }</td>
            </tr>
        `;
        return;
    }

    orders.forEach(order => {
               const row = `
    <tr>
        <td>${order.id}</td>

        <td>${order.customerName || "Customer #" + order.customerId}</td>

        <td>₹${Number(order.amount).toLocaleString("en-IN")}</td>

        <td>
            <select id="status-${order.id}">
                <option value="Pending" ${order.status === "Pending" ? "selected" : ""}>
                    Pending
                </option>

                <option value="Processing" ${order.status === "Processing" ? "selected" : ""}>
                    Processing
                </option>

                <option value="Completed" ${order.status === "Completed" ? "selected" : ""}>
                    Completed
                </option>
            </select>

            <button
                type="button"
                class="primary-btn"
                onclick="updateOrderStatus(${order.id})">
                Save Status
            </button>
        </td>
<td>
    <button
        type="button"
        class="primary-btn"
        onclick="viewOrder(${order.id})">
        View
    </button>

    <button
        type="button"
        class="cancel-btn"
        onclick="deleteOrder(${order.id})">
        Delete
    </button>
</td>
    </tr>
`;
        table.innerHTML += row;
    });
}


let allCustomers = [];

function loadCustomers() {
    const table = document.getElementById("customerTable");
    if (!table) {
        console.error("Customer table not found.");
        return;
    }

    fetch("addCustomer")
        .then(function(response) {
            if (!response.ok) {
                throw new Error("HTTP error: " + response.status);
            }
            return response.json();
        })
        .then(function(customers) {
            if (!Array.isArray(customers)) {
                throw new Error("Customers response is not a list.");
            }
            allCustomers = customers;
            filterCustomers(
                document.getElementById("customerSearch")?.value || ""
            );
        })
        .catch(function(error) {
            console.error("Error loading customers:", error);
            table.innerHTML =
                '<tr><td colspan="6">Error loading customers.</td></tr>';
        });
}

function filterCustomers(searchText) {
    const query = String(searchText || "").trim().toLocaleLowerCase();
    const filteredCustomers = allCustomers.filter(function(customer) {
        return [
            customer.id,
            customer.name,
            customer.email,
            customer.phone,
            customer.address
        ].some(function(value) {
            return String(value || "").toLocaleLowerCase().includes(query);
        });
    });

    displayCustomers(filteredCustomers);
}

function displayCustomers(customers) {
    const table = document.getElementById("customerTable");
    if (!table) {
        return;
    }

    table.replaceChildren();

    if (customers.length === 0) {
        const row = table.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 6;
        cell.textContent = allCustomers.length === 0
            ? "No customers found."
            : "No customers match your search.";
        return;
    }

    customers.forEach(function(customer) {
        const row = table.insertRow();
        [
            customer.id,
            customer.name,
            customer.email,
            customer.phone,
            customer.address
        ].forEach(function(value) {
            row.insertCell().textContent = value == null ? "" : value;
        });

        const actions = row.insertCell();
        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.className = "primary-btn";
        editButton.textContent = "Edit";
        editButton.addEventListener("click", function() {
            editCustomer(customer);
        });

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "cancel-btn";
        deleteButton.style.marginLeft = "5px";
        deleteButton.textContent = "Delete";
        deleteButton.addEventListener("click", function() {
            deleteCustomer(customer.id);
        });

        actions.append(editButton, deleteButton);
    });
}

function editCustomer(customer) {
    document.getElementById("customerId").value = customer.id;
    document.getElementById("customerName").value = customer.name || "";
    document.getElementById("customerEmail").value = customer.email || "";
    document.getElementById("customerPhone").value = customer.phone || "";
    document.getElementById("customerAddress").value = customer.address || "";
    document.getElementById("customerFormTitle").textContent = "Edit Customer";
    document.getElementById("customerSaveButtonText").textContent =
        "Update Customer";
    document.getElementById("customerForm").classList.remove("hidden");
}

function openCustomerForm() {
    closeCustomerForm();
    document.getElementById("customerForm").classList.remove("hidden");
}

function closeCustomerForm() {
    document.getElementById("customerFormElement").reset();
    document.getElementById("customerId").value = "";
    document.getElementById("customerFormTitle").textContent = "Add Customer";
    document.getElementById("customerSaveButtonText").textContent =
        "Save Customer";
    hideForm("customerForm");
}

function saveCustomer(event) {
    event.preventDefault();

    const form = document.getElementById("customerFormElement");
    const data = new URLSearchParams(new FormData(form));
    data.set("action", document.getElementById("customerId").value
        ? "edit"
        : "add");

    fetch("addCustomer", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
        .then(async function(response) {
            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.error || "Unable to save customer.");
            }
            return result;
        })
        .then(function(result) {
            alert(result.message);
            closeCustomerForm();
            loadCustomers();
        })
        .catch(function(error) {
            console.error("Save customer error:", error);
            alert(error.message);
        });
}

function deleteCustomer(customerId) {
    if (!confirm("Delete customer #" + customerId + "?")) {
        return;
    }

    const data = new URLSearchParams({
        action: "delete",
        customerId: customerId
    });

    fetch("addCustomer", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
        .then(async function(response) {
            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.error || "Unable to delete customer.");
            }
            return result;
        })
        .then(function(result) {
            alert(result.message);
            loadCustomers();
        })
        .catch(function(error) {
            console.error("Delete customer error:", error);
            alert(error.message);
        });
}

function loadCustomerDropdown() {

    fetch("addCustomer")
        .then(response => response.json())
        .then(customers => {

            const dropdown =
                document.getElementById("orderCustomer");

            if (!dropdown) {
                return;
            }

            customers.forEach(customer => {

                const option =
                    document.createElement("option");

                option.value = customer.id;
                option.textContent = customer.name;

                dropdown.appendChild(option);
            });

        })
        .catch(error => {

            console.error(
                "Error loading customer dropdown:",
                error
            );

        });
}


// ========================================
// INVENTORY MANAGEMENT
// ========================================

let allInventory = [];

// Load inventory from the database
function loadInventory() {
    const table = document.getElementById("inventoryTable");

    if (!table) return;

    table.innerHTML =
        '<tr><td colspan="5">Loading inventory...</td></tr>';

    fetch("inventory")
        .then(function(response) {
            if (!response.ok) {
                throw new Error("Failed to load inventory");
            }

            return response.json();
        })
        .then(function(inventory) {
            allInventory = inventory;
            displayInventory(allInventory);
        })
        .catch(function(error) {
            console.error("Inventory loading error:", error);

            table.innerHTML =
                '<tr><td colspan="5">Unable to load inventory.</td></tr>';
        });
}

// Display inventory and highlight low stock
function displayInventory(inventory) {
    const table = document.getElementById("inventoryTable");

    if (!table) return;

    table.innerHTML = "";

    if (inventory.length === 0) {
        table.innerHTML =
            '<tr><td colspan="5">No inventory records found.</td></tr>';
        return;
    }

    inventory.forEach(function(item) {
        const row = document.createElement("tr");

        const idCell = document.createElement("td");
        idCell.textContent = item.inventoryId;

        const productCell = document.createElement("td");
        productCell.textContent = item.productName;

        const quantityCell = document.createElement("td");
        quantityCell.textContent = item.quantity;

        if (Number(item.quantity) <= 10) {
            row.style.backgroundColor = "#fff3cd";
            row.title = "Low stock warning";

            quantityCell.textContent += " ⚠️ Low Stock";
            quantityCell.style.color = "#b45309";
            quantityCell.style.fontWeight = "bold";
        }

        const locationCell = document.createElement("td");
        locationCell.textContent = item.location;

        const actionCell = document.createElement("td");

        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.textContent = "Edit";
        editButton.className = "primary-btn";

        editButton.addEventListener("click", function() {
            editInventory(item.inventoryId, item.quantity);
        });

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.textContent = "Delete";
        deleteButton.className = "cancel-btn";
        deleteButton.style.marginLeft = "5px";

        deleteButton.addEventListener("click", function() {
            deleteInventory(item.inventoryId);
        });

        actionCell.appendChild(editButton);
        actionCell.appendChild(deleteButton);

        row.appendChild(idCell);
        row.appendChild(productCell);
        row.appendChild(quantityCell);
        row.appendChild(locationCell);
        row.appendChild(actionCell);

        table.appendChild(row);
    });
}


function loadWarehouses() {
    const table = document.getElementById("warehouseTable");

    if (!table) {
        console.error("Warehouse table not found.");
        return;
    }

    table.innerHTML =
        '<tr><td colspan="5">Loading warehouses...</td></tr>';

    fetch("warehouses")
        .then(function(response) {
            if (!response.ok) {
                throw new Error("HTTP error: " + response.status);
            }
            return response.json();
        })
        .then(function(warehouses) {
            if (!Array.isArray(warehouses)) {
                throw new Error("Warehouses response is not a list.");
            }
            allWarehouses = warehouses;
            filterWarehouses(
                document.getElementById("warehouseSearch")?.value || ""
            );
        })
        .catch(function(error) {
            console.error("Error loading warehouses:", error);
            table.innerHTML =
                '<tr><td colspan="5">Error loading warehouses. Check the browser console.</td></tr>';
        });
}

let allWarehouses = [];

function filterWarehouses(searchText) {
    const query = String(searchText || "").trim().toLocaleLowerCase();
    const filteredWarehouses = allWarehouses.filter(function(warehouse) {
        return [
            warehouse.warehouseId,
            warehouse.warehouseName,
            warehouse.location,
            warehouse.capacity
        ].some(function(value) {
            return String(value == null ? "" : value)
                .toLocaleLowerCase()
                .includes(query);
        });
    });

    displayWarehouses(filteredWarehouses);
}

function displayWarehouses(warehouses) {
    const table = document.getElementById("warehouseTable");
    if (!table) return;
    table.innerHTML = "";

    if (warehouses.length === 0) {
        table.innerHTML = allWarehouses.length === 0
            ? '<tr><td colspan="5">No warehouses found.</td></tr>'
            : '<tr><td colspan="5">No warehouses match your search.</td></tr>';
        return;
    }

    warehouses.forEach(function(warehouse) {
                const row = document.createElement("tr");

                const id = document.createElement("td");
                id.textContent = warehouse.warehouseId;

                const name = document.createElement("td");
                name.textContent = warehouse.warehouseName || "";

                const location = document.createElement("td");
                location.textContent = warehouse.location || "";

                const capacity = document.createElement("td");
                capacity.textContent = warehouse.capacity;

                const action = document.createElement("td");

                const editButton = document.createElement("button");
                editButton.type = "button";
                editButton.textContent = "Edit";
                editButton.className = "primary-btn";
                editButton.addEventListener("click", function() {
                    editWarehouse(
                        warehouse.warehouseId,
                        warehouse.warehouseName,
                        warehouse.location,
                        warehouse.capacity
                    );
                });

                const deleteButton = document.createElement("button");
                deleteButton.type = "button";
                deleteButton.textContent = "Delete";
                deleteButton.className = "cancel-btn";
                deleteButton.style.marginLeft = "5px";
                deleteButton.addEventListener("click", function() {
                    deleteWarehouse(warehouse.warehouseId);
                });

                action.append(editButton, deleteButton);

                row.append(id, name, location, capacity, action);
                table.appendChild(row);
            });
}

function editWarehouse(warehouseId, warehouseName, location, capacity) {
    document.getElementById("warehouseId").value = warehouseId;
    document.getElementById("warehouseName").value = warehouseName || "";
    document.getElementById("warehouseLocation").value = location || "";
    document.getElementById("warehouseCapacity").value = capacity;
    document.getElementById("warehouseFormTitle").textContent = "Edit Warehouse";
    document.getElementById("warehouseSaveButtonText").textContent =
        "Update Warehouse";
    document.getElementById("warehouseForm").classList.remove("hidden");
}

function closeWarehouseForm() {
    document.getElementById("warehouseFormElement").reset();
    document.getElementById("warehouseId").value = "";
    document.getElementById("warehouseFormTitle").textContent =
        "Add Warehouse";
    document.getElementById("warehouseSaveButtonText").textContent =
        "Save Warehouse";
    hideForm("warehouseForm");
}

function saveWarehouse(event) {
    event.preventDefault();

    const form = document.getElementById("warehouseFormElement");
    const data = new URLSearchParams(new FormData(form));
    data.set("action", document.getElementById("warehouseId").value
        ? "edit"
        : "add");

    fetch("warehouses", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
        .then(async function(response) {
            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Unable to save warehouse.");
            }

            return result;
        })
        .then(function(result) {
            alert(result.message);
            closeWarehouseForm();
            loadWarehouses();
        })
        .catch(function(error) {
            console.error("Save warehouse error:", error);
            alert(error.message);
        });
}

function deleteWarehouse(warehouseId) {
    if (!confirm("Delete warehouse #" + warehouseId + "?")) {
        return;
    }

    const data = new URLSearchParams({
        action: "delete",
        warehouseId: warehouseId
    });

    fetch("warehouses", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
        .then(async function(response) {
            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Unable to delete warehouse.");
            }

            return result;
        })
        .then(function(result) {
            alert(result.message);
            loadWarehouses();
        })
        .catch(function(error) {
            console.error("Delete warehouse error:", error);
            alert(error.message);
        });
}

function loadInventoryProducts() {
const dropdown = document.getElementById("inventoryProduct");


if (!dropdown) return;

dropdown.innerHTML = '<option value="">Loading products...</option>';

fetch("addProduct")
    .then(function(response) {
        if (!response.ok) {
            throw new Error("HTTP error: " + response.status);
        }
        return response.json();
    })
    .then(function(products) {
        dropdown.innerHTML = '<option value="">Select Product</option>';

        products.forEach(function(product) {
            const option = document.createElement("option");

            option.value = product.id;
            option.textContent = product.name + " (" + product.sku + ")";

            dropdown.appendChild(option);
        });

        if (products.length === 0) {
            dropdown.innerHTML = '<option value="">No products available</option>';
        }
    })
    .catch(function(error) {
        console.error("Error loading products:", error);
        dropdown.innerHTML = '<option value="">Failed to load products</option>';
    });


}

function loadInventoryWarehouses() {
const dropdown = document.getElementById("inventoryLocation");


if (!dropdown) return;

dropdown.innerHTML = '<option value="">Loading warehouses...</option>';

fetch("warehouses")
    .then(function(response) {
        if (!response.ok) {
            throw new Error("HTTP error: " + response.status);
        }
        return response.json();
    })
    .then(function(warehouses) {
        dropdown.innerHTML = '<option value="">Select Warehouse</option>';

        warehouses.forEach(function(warehouse) {
            const option = document.createElement("option");

            option.value = warehouse.warehouseId;
            option.textContent =
                warehouse.warehouseName + " - " + warehouse.location;

            dropdown.appendChild(option);
        });

        if (warehouses.length === 0) {
            dropdown.innerHTML = '<option value="">No warehouses available</option>';
        }
    })
    .catch(function(error) {
        console.error("Error loading warehouses:", error);
        dropdown.innerHTML = '<option value="">Failed to load warehouses</option>';
    });


}

function editInventory(inventoryId, currentQuantity) {

    const newQuantity = prompt(
        "Enter the new quantity:",
        currentQuantity
    );

    if (newQuantity === null) {
        return;
    }

    const quantity = Number(newQuantity);

    if (!Number.isInteger(quantity) || quantity <= 0) {
        alert("Please enter a positive whole number.");
        return;
    }

    const data = new URLSearchParams();

    data.append("action", "edit");
    data.append("inventoryId", inventoryId);
    data.append("quantity", quantity);

    fetch("inventory", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error("Unable to update stock.");
        }

        return response.json();
    })
    .then(function(result) {
        alert(result.message);
        loadInventory();
    })
    .catch(function(error) {
        console.error(error);
        alert("Could not update stock.");
    });
}

function deleteInventory(inventoryId) {

    const confirmed = confirm(
        "Are you sure you want to delete this inventory record?"
    );

    if (!confirmed) {
        return;
    }

    const data = new URLSearchParams();

    data.append("action", "delete");
    data.append("inventoryId", inventoryId);

    fetch("inventory", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error("Unable to delete inventory.");
        }

        return response.json();
    })
    .then(function(result) {
        alert(result.message);
        loadInventory();
    })
    .catch(function(error) {
        console.error(error);
        alert("Could not delete inventory.");
    });
}

// ========================================
// DISPATCH MANAGEMENT
// ========================================

// Load all dispatch records
let allDispatches = [];

function loadDispatches() {

    fetch("dispatches")
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to load dispatches");
            }

            return response.json();
        })
        .then(dispatches => {
            if (!Array.isArray(dispatches)) {
                throw new Error("Dispatches response is not a list.");
            }
            allDispatches = dispatches;
            filterDispatches(
                document.getElementById("dispatchSearch")?.value || ""
            );
        })
        .catch(error => {
            console.error("Dispatch loading error:", error);
            const table = document.getElementById("dispatchTable");
            if (table) {
                table.innerHTML = `
                    <tr>
                        <td colspan="6">Unable to load dispatches.</td>
                    </tr>
                `;
            }
        });
}

function filterDispatches(searchText) {
    const query = String(searchText || "").trim().toLocaleLowerCase();
    const filteredDispatches = allDispatches.filter(item =>
        [
            item.dispatchId,
            item.orderId,
            item.carrier,
            item.trackingNumber,
            item.status
        ].some(value =>
            String(value == null ? "" : value)
                .toLocaleLowerCase()
                .includes(query)
        )
    );

    displayDispatches(filteredDispatches);
}

function displayDispatches(dispatches) {
    const table = document.getElementById("dispatchTable");
    if (!table) return;
    table.innerHTML = "";

    if (dispatches.length === 0) {
        table.innerHTML = `
            <tr>
                <td colspan="6">${
                    allDispatches.length === 0
                        ? "No dispatch records found."
                        : "No dispatches match your search."
                }</td>
            </tr>
        `;
        return;
    }

    dispatches.forEach(item => {

                const row = document.createElement("tr");

                row.innerHTML = `
                    <td>${item.dispatchId}</td>
                    <td>${item.orderId}</td>
                    <td>${escapeDispatchHTML(item.carrier)}</td>
                    <td>${escapeDispatchHTML(item.trackingNumber)}</td>
                    <td>${escapeDispatchHTML(item.status)}</td>
                    <td>
                        <button
                            class="primary-btn"
                            onclick="editDispatch(${item.dispatchId},
                                ${item.orderId},
                                '${escapeDispatchJS(item.carrier)}',
                                '${escapeDispatchJS(item.trackingNumber)}',
                                '${escapeDispatchJS(item.status)}')">
                            Edit
                        </button>

                        <button
                            class="cancel-btn"
                            onclick="deleteDispatch(${item.dispatchId})">
                            Delete
                        </button>
                    </td>
                `;

                table.appendChild(row);

            });
}


// Add a new dispatch
function addDispatch(event) {

    event.preventDefault();

    const orderId = document.getElementById("dispatchOrder").value;
    const carrier = document.getElementById("dispatchCarrier").value.trim();
    const tracking = document.getElementById("dispatchTracking").value.trim();
    const status = document.getElementById("dispatchStatus").value;

    if (!orderId || !carrier || !tracking) {
        alert("Please fill in all dispatch details.");
        return;
    }

    const data = new URLSearchParams();

    data.append("action", "add");
    data.append("orderId", orderId);
    data.append("carrier", carrier);
    data.append("tracking", tracking);
    data.append("status", status);

    fetch("dispatches", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
        .then(async response => {

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Unable to add dispatch.");
            }

            return result;

        })
        .then(result => {

            alert(result.message);

            document.querySelector("#dispatchForm form").reset();

            hideForm("dispatchForm");

            loadDispatches();

        })
        .catch(error => {

            console.error("Add dispatch error:", error);

            alert(error.message);

        });
}


// Edit an existing dispatch
function editDispatch(dispatchId, orderId, carrier, tracking, status) {

    const newOrderId = prompt("Enter Order ID:", orderId);

    if (newOrderId === null) return;

    if (!newOrderId.trim() || !Number.isInteger(Number(newOrderId)) ||
        Number(newOrderId) <= 0) {

        alert("Please enter a valid Order ID.");
        return;
    }

    const newCarrier = prompt("Enter Delivery Carrier:", carrier);

    if (newCarrier === null) return;

    if (!newCarrier.trim()) {
        alert("Carrier cannot be empty.");
        return;
    }

    const newTracking = prompt("Enter Tracking Number:", tracking);

    if (newTracking === null) return;

    if (!newTracking.trim()) {
        alert("Tracking number cannot be empty.");
        return;
    }

    const newStatus = prompt(
        "Enter Status (Ready, Shipped, Delivered):",
        status
    );

    if (newStatus === null) return;

    const allowedStatuses = ["Ready", "Shipped", "Delivered"];

    if (!allowedStatuses.includes(newStatus.trim())) {
        alert("Status must be Ready, Shipped, or Delivered.");
        return;
    }

    const data = new URLSearchParams();

    data.append("action", "edit");
    data.append("dispatchId", dispatchId);
    data.append("orderId", newOrderId);
    data.append("carrier", newCarrier.trim());
    data.append("tracking", newTracking.trim());
    data.append("status", newStatus.trim());

    fetch("dispatches", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
        .then(async response => {

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Unable to update dispatch.");
            }

            return result;

        })
        .then(result => {

            alert(result.message);

            loadDispatches();

        })
        .catch(error => {

            console.error("Edit dispatch error:", error);

            alert(error.message);

        });
}


// Delete a dispatch
function deleteDispatch(dispatchId) {

    const confirmed = confirm(
        "Are you sure you want to delete dispatch #" + dispatchId + "?"
    );

    if (!confirmed) return;

    const data = new URLSearchParams();

    data.append("action", "delete");
    data.append("dispatchId", dispatchId);

    fetch("dispatches", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
        .then(async response => {

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Unable to delete dispatch.");
            }

            return result;

        })
        .then(result => {

            alert(result.message);

            loadDispatches();

        })
        .catch(error => {

            console.error("Delete dispatch error:", error);

            alert(error.message);

        });
}


// Escape database values before displaying them in HTML
function escapeDispatchHTML(value) {

    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);

}


// Escape values used inside the Edit button's JavaScript arguments
function escapeDispatchJS(value) {

    return String(value ?? "")
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/\r/g, "\\r")
        .replace(/\n/g, "\\n")
        .replace(/\u2028/g, "\\u2028")
        .replace(/\u2029/g, "\\u2029");

}

// ========================================
// INVENTORY SEARCH
// ========================================

document.addEventListener("DOMContentLoaded", function() {

    const searchBox = document.getElementById("inventorySearch");

    if (!searchBox) return;

    searchBox.addEventListener("input", function() {

        const searchText = this.value.trim().toLowerCase();

        const filteredInventory = allInventory.filter(function(item) {

            return String(item.productName ?? "")
                       .toLowerCase()
                       .includes(searchText)

                || String(item.location ?? "")
                       .toLowerCase()
                       .includes(searchText)

                || String(item.inventoryId ?? "")
                       .includes(searchText);

        });

        displayInventory(filteredInventory);

    });

});

// ========================================
// ORDER DETAILS
// ========================================

function viewOrder(orderId) {

    const modal = document.getElementById("orderDetailsModal");
    const content = document.getElementById("orderDetailsContent");

    modal.classList.remove("hidden");
    content.textContent = "Loading order details...";

    fetch("addOrder")
        .then(function(response) {

            if (!response.ok) {
                throw new Error("Unable to fetch orders.");
            }

            return response.json();

        })
        .then(function(orders) {

            const order = orders.find(function(item) {
                return Number(item.id) === Number(orderId);
            });

            if (!order) {
                throw new Error("Order not found.");
            }

            const details = [
                ["Order ID", order.id],
                ["Customer", order.customerName || "Customer #" + order.customerId],
                ["Order Date", order.orderDate || "Not available"],
                ["Amount", "₹" + Number(order.amount).toLocaleString("en-IN", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                })],
                ["Status", order.status]
            ];

            content.replaceChildren();

            details.forEach(function(detail) {

                const row = document.createElement("p");
                const label = document.createElement("strong");

                label.textContent = detail[0] + ": ";

                row.appendChild(label);
                row.appendChild(document.createTextNode(String(detail[1])));

                content.appendChild(row);

            });

        })
        .catch(function(error) {

            console.error("Order details error:", error);
            content.textContent = "Unable to load order details.";

        });

}


function closeOrderDetails() {

    document
        .getElementById("orderDetailsModal")
        .classList.add("hidden");

}

function updateOrderStatus(orderId) {


const dropdown = document.getElementById("status-" + orderId);
const status = dropdown.value;

const data = new URLSearchParams();

data.append("action", "updateStatus");
data.append("orderId", orderId);
data.append("status", status);

fetch("addOrder", {
    method: "POST",
    headers: {
        "Content-Type": "application/x-www-form-urlencoded"
    },
    body: data.toString()
})
.then(async function(response) {

    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.message || "Unable to update status.");
    }

    return result;
})
.then(function(result) {

    alert(result.message);
    loadOrders();

})
.catch(function(error) {

    console.error("Order status update error:", error);
    alert(error.message);

});


}


// ========================================
// DELETE ORDER
// ========================================


function deleteOrder(orderId) {
    const confirmed = confirm(
        "Are you sure you want to permanently delete order #" + orderId + "?"
    );

    if (!confirmed) return;

    const data = new URLSearchParams();
    data.append("action", "delete");
    data.append("orderId", orderId);

    fetch("addOrder", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: data.toString()
    })
    .then(async function(response) {
        // Read the response as text first
        const text = await response.text();

        // Convert JSON text into an object if possible
        let result;

        try {
            result = JSON.parse(text);
        } catch (error) {
            throw new Error(
                "Server response: " + text
            );
        }

        if (!response.ok) {
            throw new Error(
                result.message || "Unable to delete order."
            );
        }

        return result;
    })
    .then(function(result) {
        alert(result.message || "Order deleted successfully.");
        loadOrders();
    })
    .catch(function(error) {
        console.error("Delete order error:", error);
        alert(error.message);
    });
}




function openDispatchForm() {
    const form = document.getElementById("dispatchForm");

    if (!form) {
        console.error("Dispatch form element 'dispatchForm' was not found.");
        alert("Dispatch form not found. Please check dispatch.html.");
        return;
    }

    form.classList.remove("hidden");
    form.reset();
}


// ================= DISPATCH FORM =================

function openDispatchForm() {
    const formBox = document.getElementById("dispatchForm");
    const form = document.getElementById("dispatchFormElement");
    const title = document.getElementById("dispatchFormTitle");
    const saveButton = document.getElementById("dispatchSaveButton");

    if (!formBox || !form) {
        console.error("Dispatch form elements not found.");
        alert("Unable to open dispatch form.");
        return;
    }

    form.reset();

    document.getElementById("dispatchId").value = "";

    if (title) {
        title.textContent = "Add Dispatch";
    }

    if (saveButton) {
        saveButton.textContent = "Save Dispatch";
    }

    formBox.classList.remove("hidden");
}


function closeDispatchForm() {
    const formBox = document.getElementById("dispatchForm");

    if (formBox) {
        formBox.classList.add("hidden");
    }
}



function loadOrdersForDispatch() {
    const orderDropdown = document.getElementById("dispatchOrder");

    if (!orderDropdown) {
        console.error("Dispatch order dropdown not found.");
        return;
    }

    fetch("addOrder")
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch orders: " + response.status);
            }

            return response.json();
        })
        .then(orders => {
            orderDropdown.replaceChildren(
                new Option("Select Order", "")
            );

            if (!Array.isArray(orders)) {
                throw new Error("Orders response is not a list.");
            }

            const availableOrders = orders.filter(order =>
                order && order.id != null
            );

            if (availableOrders.length === 0) {
                orderDropdown.add(new Option("No orders available", ""));
                return;
            }

            availableOrders.forEach(order => {
                const customer = order.customerName ||
                    "Customer #" + order.customerId;
                const amount = Number(order.amount).toLocaleString("en-IN");
                const status = order.status || "Unknown";
                const option = new Option(
                    "Order #" + order.id + " - " + customer +
                    " - ₹" + amount + " (" + status + ")",
                    order.id
                );

                orderDropdown.add(option);
            });
        })
        .catch(error => {
            console.error("Error loading orders for dispatch:", error);
            orderDropdown.replaceChildren(
                new Option("Unable to load orders", "")
            );
        });
}
