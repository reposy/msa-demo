document.addEventListener('DOMContentLoaded', function() {
    const generateDataBtn = document.getElementById('generateDataBtn');
    const refreshBtn = document.getElementById('refreshBtn');

    generateDataBtn.addEventListener('click', function() {
        const generateCount = parseInt(document.getElementById('generateCount').value, 10) || 10;
        generateData(generateCount).then(() => {
            refreshData();
        });
    });

    refreshBtn.addEventListener('click', function() {
        refreshData();
    });

    // 초기 데이터 로드
    refreshData();
});

// GraphQL API 호출 공통 함수
function callGraphQL(query, variables = {}) {
    return fetch('http://localhost:8084/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query, variables })
    }).then(res => res.json());
}

// 대량 랜덤 데이터 생성 Mutation 호출
function generateData(count) {
    const mutation = `
    mutation GenerateData($count: Int!) {
      generateUsers(count: $count) {
        id
        name
        email
      }
      generateProducts(count: $count) {
        id
        name
        description
        price
        stock
      }
      generateOrders(count: $count) {
        id
        userId
        productId
        quantity
        status
      }
    }
  `;
    return callGraphQL(mutation, { count }).then(data => {
        console.log('Data generated:', data);
    }).catch(err => console.error('Error generating data:', err));
}

// 전체 데이터 및 최근 데이터 조회 Query 호출
function refreshData() {
    const recentCount = parseInt(document.getElementById('recentCount').value, 10) || 10;
    const query = `
    query GetData($recentCount: Int!) {
      users { id }
      products { id }
      orders { id }
      recentUsers(count: $recentCount) {
        id
        name
        email
      }
      recentProducts(count: $recentCount) {
        id
        name
        price
        stock
      }
      recentOrders(count: $recentCount) {
        id
        userId
        productId
        quantity
        status
      }
    }
  `;
    callGraphQL(query, { recentCount }).then(data => {
        if (data.data) {
            updateSummary(data.data);
            updateTables(data.data);
        }
    }).catch(err => console.error('Error refreshing data:', err));
}

// 업데이트: 데이터 요약 영역 (전체 데이터 개수)
function updateSummary(data) {
    const userCount = data.users ? data.users.length : 0;
    const productCount = data.products ? data.products.length : 0;
    const orderCount = data.orders ? data.orders.length : 0;
    document.getElementById('userCount').textContent = userCount;
    document.getElementById('productCount').textContent = productCount;
    document.getElementById('orderCount').textContent = orderCount;
}

// 업데이트: 테이블에 최근 데이터 렌더링
function updateTables(data) {
    // Users 테이블 업데이트
    const usersTable = document.getElementById('usersTable');
    usersTable.innerHTML = '';
    if (data.recentUsers) {
        data.recentUsers.forEach(user => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td class="py-2 px-4 border">${user.id}</td>
                      <td class="py-2 px-4 border">${user.name}</td>
                      <td class="py-2 px-4 border">${user.email}</td>`;
            usersTable.appendChild(tr);
        });
    }

    // Products 테이블 업데이트
    const productsTable = document.getElementById('productsTable');
    productsTable.innerHTML = '';
    if (data.recentProducts) {
        data.recentProducts.forEach(product => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td class="py-2 px-4 border">${product.id}</td>
                      <td class="py-2 px-4 border">${product.name}</td>
                      <td class="py-2 px-4 border">${product.price}</td>
                      <td class="py-2 px-4 border">${product.stock}</td>`;
            productsTable.appendChild(tr);
        });
    }

    // Orders 테이블 업데이트
    const ordersTable = document.getElementById('ordersTable');
    ordersTable.innerHTML = '';
    if (data.recentOrders) {
        data.recentOrders.forEach(order => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td class="py-2 px-4 border">${order.id}</td>
                      <td class="py-2 px-4 border">${order.userId}</td>
                      <td class="py-2 px-4 border">${order.productId}</td>
                      <td class="py-2 px-4 border">${order.quantity}</td>
                      <td class="py-2 px-4 border">${order.status}</td>`;
            ordersTable.appendChild(tr);
        });
    }
}