<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

        <c:forEach var="customer" items="${customers}">
            <tr>
                <td>${customer.id}</td>
                <td>${customer.name}</td>
                <td>${customer.email}</td>
                <td>${customer.phoneNumber}</td>
                <td>${customer.loyaltyPoints}</td>
                <td>
                    <button class="editCustomerBtn"
                        data-customer='{"id":"${customer.id}", "name":"${customer.name}", "email":"${customer.email}", "phoneNumber":"${customer.phoneNumber}", "loyaltyPoints":"${customer.loyaltyPoints}", "version":"${customer.version}"}'>
                        Edit
                    </button>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${customers == null || customers.isEmpty()}">
            <tr>
                <td colspan="6">No customers found.</td>
            </tr>
        </c:if>