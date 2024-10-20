<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Generate Report</title>
            <script src="https://cdn.tailwindcss.com"></script>
            <script>
                document.addEventListener('DOMContentLoaded', function () {
                    const reportChoice = document.getElementById('reportChoice');
                    const dateField = document.getElementById('dateField');
                    const dateLabel = document.getElementById('dateLabel');

                    // Function to toggle the date field visibility and label based on selected report
                    function toggleDateField() {
                        const selectedValue = reportChoice.value;
                        // Show date field for Daily Sales (1), Reshelved Items (2), and Bill Report (5)
                        if (selectedValue === '1' || selectedValue === '2' || selectedValue === '5') {
                            dateField.style.display = 'block';
                            if (selectedValue === '5') {
                                dateLabel.innerHTML = 'Select Date (Optional)';
                            } else {
                                dateLabel.innerHTML = 'Select Date';
                            }
                        } else {
                            dateField.style.display = 'none';
                        }
                    }

                    // Initial call to set the visibility based on the default selection
                    toggleDateField();

                    // Event listener to check for changes in the dropdown
                    reportChoice.addEventListener('change', toggleDateField);
                });
            </script>
        </head>

        <body class="bg-gray-100 text-gray-800 font-sans min-h-screen">
            <div class="container mx-auto py-10">
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

                <h2 class="text-3xl font-bold mb-6">Report Generation</h2>

                <!-- Form for selecting the report -->
                <form action="report" method="post" class="bg-white shadow-md rounded-lg p-6 mb-8">
                    <!-- Hidden action parameter -->
                    <input type="hidden" name="action" value="2"> <!-- '2' corresponds to generating a report -->

                    <div class="mb-4">
                        <label for="reportChoice" class="block text-sm font-medium text-gray-700">Select Report
                            Type</label>
                        <select name="reportChoice" id="reportChoice"
                            class="mt-1 p-2 block w-full border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500">
                            <option value="1">Daily Sales Report</option>
                            <option value="2">Reshelved Items Report</option>
                            <option value="3">Reorder Stock Report</option>
                            <option value="4">Stock Report</option>
                            <option value="5">Bill Report</option>
                        </select>
                    </div>

                    <!-- Date field (conditionally displayed) -->
                    <div id="dateField" class="mb-4">
                        <label for="reportDate" id="dateLabel" class="block text-sm font-medium text-gray-700">Select
                            Date</label>
                        <input type="date" name="reportDate" id="reportDate"
                            class="mt-1 block w-full border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>

                    <button type="submit"
                        class="bg-indigo-600 hover:bg-indigo-800 text-white font-bold py-2 px-4 rounded-lg shadow-md transition duration-300">
                        Generate Report
                    </button>
                </form>

                <!-- Display the generated report -->
                <c:if test="${not empty reportData}">
                    <h3 class="text-2xl font-semibold mb-4">Generated Report:</h3>
                    <div class="overflow-x-auto bg-gray-50 p-4 rounded-lg shadow-md border border-gray-200">
                        <pre class="min-w-full whitespace-pre">${reportData}</pre>
                    </div>
                </c:if>
            </div>
        </body>

        </html>