<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

        <script type="text/javascript">
            // Creating a JavaScript object to hold Bill details
            var bill = {
                billId: "${bill.billId}",
                customerId: "${bill.customerId}",
                totalAmount: "${bill.totalAmount}",
                cashTendered: "${bill.cashTendered}",
                changeGiven: "${bill.changeGiven}",
                billDate: "${bill.billDate}",
                discountAmount: "${bill.discountAmount}",
                taxAmount: "${bill.taxAmount}",
                finalPrice: "${bill.finalPrice}",
                loyaltyPointsUsed: "${bill.loyaltyPointsUsed}"
            };

            // Logging the bill object to the console
            console.log(bill);
        </script>

        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Billing System</title>
            <script src="https://cdn.tailwindcss.com"></script>
        </head>

        <body class="bg-gray-100 text-gray-800 font-sans min-h-screen">
            <div class="container mx-auto py-10 px-6">
                <!-- Back to Main Menu link with arrow -->
                <a href="<c:url value='/' />"
                    class="text-blue-600 hover:underline mb-6 inline-flex items-center font-medium">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"
                        xmlns="http://www.w3.org/2000/svg">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7">
                        </path>
                    </svg>
                    Return to Main Menu
                </a>

                <h2 class="text-3xl font-bold mb-6 text-gray-900">Billing System</h2>

                <!-- Success/ Error Messages -->
                <!-- Display success messages -->
                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="bg-green-100 text-green-700 border border-green-400 p-4 mb-4 rounded">
                        <p>${successMessage}</p>
                    </div>
                    <c:remove var="successMessage" scope="session" />
                </c:if>


                <c:if test="${not empty sessionScope.errorMessage}">
                    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4">
                        ${errorMessage}
                    </div>
                    <c:remove var="errorMessage" scope="session" />
                </c:if>

                <!-- Step 1: Enter Customer Phone Number (Optional) -->
                <c:if test="${empty sessionScope.customerPhone && sessionScope.skipPhone != true}">
                    <h3 class="text-2xl font-semibold mb-4">Enter Customer Phone Number or Skip</h3>
                    <form action="billing" method="post" class="space-y-4 bg-white p-6 rounded-lg shadow-md">
                        <div>
                            <label for="customerPhone" class="block text-sm font-medium text-gray-700">Customer
                                Phone
                                Number:</label>
                            <input type="text" name="customerPhone" id="customerPhone" placeholder="Enter phone number"
                                class="mt-1 block w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                        </div>
                        <input type="hidden" name="action" value="1">
                        <input type="hidden" name="operation" value="enterPhoneNumber">
                        <div class="flex space-x-2 justify-end">
                            <button type="submit"
                                class="bg-indigo-600 hover:bg-indigo-800 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                                Next
                            </button>
                            <button type="submit" name="skipPhone" value="true"
                                class="bg-gray-500 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                                Skip Phone Number
                            </button>
                        </div>
                    </form>
                </c:if>

                <!-- Show Customer Name if the phone number is entered and customer is found -->
                <c:if test="${not empty sessionScope.customerName}">
                    <h4 class="text-2xl font-semibold mt-6 mb-4">Welcome, ${sessionScope.customerName}</h4>
                </c:if>

                <!-- Step 2: Add Items to the Bill -->
                <c:if test="${not empty sessionScope.customerPhone || sessionScope.skipPhone eq true}">
                    <c:if test="${empty sessionScope.addedItemsDone}">
                        <h3 class="text-xl font-semibold mb-4">Add Items to Bill</h3>

                        <form action="billing" method="post" class="space-y-4 bg-white p-6 rounded-lg shadow-md">
                            <div>
                                <label for="itemCode" class="block text-sm font-medium text-gray-700">Item
                                    Code:</label>
                                <input type="text" name="itemCode" id="itemCode" placeholder="Enter item code"
                                    class="mt-1 block w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                    required>
                            </div>
                            <div>
                                <label for="quantity" class="block text-sm font-medium text-gray-700">Quantity:</label>
                                <input type="number" name="quantity" id="quantity" min="1"
                                    class="mt-1 block w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                    required>
                            </div>
                            <input type="hidden" name="action" value="1">
                            <input type="hidden" name="operation" value="addItem">
                            <div class="flex justify-end">
                                <button type="submit"
                                    class="bg-indigo-600 hover:bg-indigo-800 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                                    Add Item
                                </button>
                            </div>
                        </form>
                    </c:if>

                    <!-- Display Added Items -->
                    <c:if test="${not empty sessionScope.addedItems}">
                        <h4 class="text-xl font-semibold mt-6 mb-4">Current Items in Bill</h4>

                        <!-- Table Header -->
                        <div class="grid grid-cols-5 gap-4 bg-gray-200 text-gray-700 p-3 rounded-lg font-semibold">
                            <div>Item Code</div>
                            <div>Item Name</div>
                            <div>Quantity</div>
                            <div>Price</div>
                            <div>Actions</div>
                        </div>

                        <!-- Table Body -->
                        <ul class="space-y-2 bg-white p-6 rounded-lg shadow-md">
                            <!-- Loop through transactions -->
                            <c:forEach var="transaction" items="${sessionScope.addedItems}"
                                varStatus="transactionStatus">
                                <c:forEach var="itemQuantity" items="${transaction.itemQuantities}">
                                    <li class="grid grid-cols-5 gap-4 items-center border-b py-2">
                                        <!-- Item Details -->
                                        <div>${itemQuantity.item.code}</div>
                                        <div>${itemQuantity.item.name}</div>
                                        <div>${itemQuantity.quantity}</div>
                                        <div>${itemQuantity.item.price}</div>

                                        <!-- Remove Button -->
                                        <div>
                                            <form action="billing" method="post" style="display:inline;">
                                                <input type="hidden" name="action" value="1">
                                                <input type="hidden" name="operation" value="removeItem">
                                                <input type="hidden" name="itemCode" value="${itemQuantity.item.code}">
                                                <input type="hidden" name="transactionIndex"
                                                    value="${transactionStatus.index}">
                                                <button type="submit"
                                                    class="text-red-600 hover:text-red-700 font-bold transition duration-300">
                                                    Remove
                                                </button>
                                            </form>
                                        </div>
                                    </li>
                                </c:forEach>
                            </c:forEach>
                        </ul>
                    </c:if>


                    <c:if test="${not empty sessionScope.addedItems}">
                        <form action="billing" method="post" class="mt-4">
                            <input type="hidden" name="action" value="1">
                            <input type="hidden" name="operation" value="doneAddingItems">
                            <div class="flex justify-end">
                                <button type="submit"
                                    class="bg-indigo-600 hover:bg-indigo-800 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                                    Done Adding Items
                                </button>
                            </div>
                        </form>
                    </c:if>
                </c:if>

                <!-- Step 3: Apply Loyalty Points and Discounts -->
                <c:if test="${not empty sessionScope.addedItemsDone && not empty sessionScope.addedItems}">
                    <h3 class="text-2xl font-semibold mb-4">Apply Loyalty Points and Discounts</h3>
                    <form action="billing" method="post" class="space-y-4 bg-white p-6 rounded-lg shadow-md">
                        <!-- Conditionally display loyalty points input if the customer phone number is provided -->
                        <c:if test="${not empty sessionScope.customerPhone && sessionScope.skipPhone != true}">
                            <div>
                                <label for="loyaltyPoints" class="block text-sm font-medium text-gray-700">
                                    Loyalty Points
                                    <span class="font-bold">(Available:
                                        ${sessionScope.customerLoyaltyPoints}):</span>
                                </label>
                                <input type="number" name="loyaltyPoints" id="loyaltyPoints"
                                    placeholder="Enter loyalty points"
                                    value="${sessionScope.loyaltyPoints != null ? loyaltyPoints : '0'}" min="0"
                                    max="${sessionScope.customerLoyaltyPoints}"
                                    class="mt-1 block w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                            </div>
                        </c:if>

                        <div>
                            <label for="discountRate" class="block text-sm font-medium text-gray-700">Discount Rate
                                (0-100%):</label>
                            <input type="number" name="discountRate" id="discountRate" placeholder="Enter discount rate"
                                value="${sessionScope.discountRate != null ? discountRate : '0'}" min="0" max="100"
                                required
                                class="mt-1 block w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                        </div>
                        <input type="hidden" name="action" value="1">
                        <input type="hidden" name="operation" value="applyDiscountAndLoyaltyPoints">
                        <div class="flex justify-end">
                            <button type="submit"
                                class="bg-indigo-600 hover:bg-indigo-800 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                                Apply Discounts
                            </button>
                        </div>
                    </form>
                </c:if>

                <!-- Step 4: Display Total and Enter Cash Received -->
                <c:if
                    test="${not empty sessionScope.discountsApplied && empty sessionScope.errorMessage && sessionScope.finalAmount > 0 && not empty sessionScope.addedItemsDone && not empty sessionScope.addedItems}">
                    <h3 class="text-xl font-semibold mt-6 mb-4">Total Amount: ${sessionScope.finalAmount}</h3>
                    <form action="billing" method="post" class="space-y-4 bg-white p-6 rounded-lg shadow-md">
                        <div>
                            <label for="cashReceived" class="block text-sm font-medium text-gray-700">Cash
                                Received:</label>
                            <input type="number" name="cashReceived" id="cashReceived" placeholder="Enter cash received"
                                value="${sessionScope.cashReceived}"
                                class="mt-1 block w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                                required>
                        </div>
                        <input type="hidden" name="action" value="1">
                        <input type="hidden" name="operation" value="calculateChange">
                        <div class="flex justify-end">
                            <button type="submit"
                                class="bg-indigo-600 hover:bg-indigo-800 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                                Calculate Change
                            </button>
                        </div>
                    </form>
                </c:if>

                <!-- Step 5: Display Change and Finalize Transaction -->
                <c:if
                    test="${not empty sessionScope.changeAmount && not empty sessionScope.addedItems && not empty sessionScope.addedItemsDone && not empty sessionScope.bill}">
                    <h3 class="text-xl font-semibold mt-6 mb-4">Change to Return: ${sessionScope.changeAmount}</h3>

                    <div class="bg-white p-6 rounded-lg shadow-md mt-6">
                        <h3 class="text-3xl font-semibold mb-6 text-gray-800">SYOS Bill Details</h3>

                        <!-- Customer Information -->
                        <div class="mb-6 text-lg">
                            <p class="text-lg"><strong>Customer:</strong> ${sessionScope.customerName}</p>
                            <p class="text-lg"><strong>Date:</strong> ${sessionScope.bill.formattedBillDate}</p>
                        </div>

                        <!-- Items Table -->
                        <h4 class="text-xl font-semibold mb-4 text-gray-700">Items Purchased</h4>
                        <div class="overflow-hidden border rounded-lg">
                            <table class="min-w-full bg-white">
                                <thead>
                                    <tr class="w-full bg-gray-200 text-gray-700 font-semibold text-left p-3">
                                        <th class="text-lg py-3 px-6">Item Code</th>
                                        <th class="text-lg py-3 px-6">Item Name</th>
                                        <th class="text-lg py-3 px-6">Quantity</th>
                                        <th class="text-lg py-3 px-6">Total Price</th>
                                    </tr>
                                </thead>
                                <tbody class="text-gray-600">
                                    <c:forEach var="transaction" items="${sessionScope.bill.transactions}">
                                        <c:forEach var="itemQuantity" items="${transaction.items}">
                                            <tr class="border-b">
                                                <td class="text-lg py-3 px-6">${itemQuantity.item.code}</td>
                                                <td class="text-lg py-3 px-6">${itemQuantity.item.name}</td>
                                                <td class="text-lg py-3 px-6">${itemQuantity.quantity}</td>
                                                <td class="text-lg py-3 px-6">${itemQuantity.item.price *
                                                    itemQuantity.quantity}</td>
                                            </tr>
                                        </c:forEach>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <!-- Price Summary -->
                        <div class="mt-6 bg-gray-50 p-6 rounded-lg">
                            <h4 class="text-xl font-semibold mb-4 text-gray-700">Summary</h4>
                            <div class="grid grid-cols-4 text-lg">
                                <p class="col-span-3 text-left"><strong>Sub Total:</strong></p>
                                <p class="pl-8">${sessionScope.bill.totalAmount}</p>

                                <p class="col-span-3 text-left"><strong>Discount:</strong></p>
                                <p class="pl-8">${sessionScope.bill.discountAmount}</p>

                                <p class="col-span-3 text-left"><strong>Tax:</strong></p>
                                <p class="pl-8">${sessionScope.bill.taxAmount}</p>

                                <c:if test="${not empty sessionScope.customerPhone}">
                                    <p class="col-span-3 text-left"><strong>Loyalty Points Used:</strong></p>
                                    <p class="pl-8">${sessionScope.bill.loyaltyPointsUsed}</p>
                                </c:if>

                                <p class="col-span-3 text-left"><strong>Final Price:</strong></p>
                                <p class="pl-8 font-semibold">${sessionScope.bill.finalPrice}</p>

                                <p class="col-span-3 text-left"><strong>Cash Tendered:</strong></p>
                                <p class="pl-8">${sessionScope.cashReceived}</p>

                                <p class="col-span-3 text-left"><strong>Change Given:</strong></p>
                                <p class="pl-8 text-green-600 font-semibold">${sessionScope.changeAmount}</p>
                            </div>
                        </div>

                    </div>


                    <form action="billing" method="post">
                        <input type="hidden" name="action" value="1">
                        <input type="hidden" name="operation" value="finalizeTransaction">
                        <div class="flex justify-end mt-2">
                            <button type="submit"
                                class="bg-green-700 hover:bg-green-800 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                                Finalize and Save Transaction
                            </button>
                        </div>
                    </form>
                </c:if>

                <!-- Exit Billing -->
                <form action="billing" method="post" class="mt-4">
                    <input type="hidden" name="action" value="1">
                    <input type="hidden" name="operation" value="exitBilling">
                    <div class="flex justify-end">
                        <button type="submit"
                            class="bg-red-700 hover:bg-red-800 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                            Cancel Billing
                        </button>
                    </div>
                </form>
            </div>
        </body>

        </html>