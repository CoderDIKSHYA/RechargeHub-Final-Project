# --- RECHARGEHUB FINAL DEPLOYMENT SCRIPT (POWERSHELL) ---
# This script builds, tags, and prepares your microservices for the Cloud.

$DOCKER_USERNAME = "dikshya1801"

echo "🚀 Starting Enterprise Build Process..."

# 1. Build and Tag Eureka Server
cd eureka-server
docker build -t $DOCKER_USERNAME/recharge-eureka-server:latest .
cd ..

# 2. Build and Tag Gateway
cd gatewayservice
docker build -t $DOCKER_USERNAME/recharge-gateway:latest .
cd ..

# 3. Build and Tag User Service
cd user-service
docker build -t $DOCKER_USERNAME/recharge-user-service:latest .
cd ..

# 4. Build and Tag Operator Service
cd operator-service
docker build -t $DOCKER_USERNAME/recharge-operator-service:latest .
cd ..

# 5. Build and Tag Payment Service
cd payment-service
docker build -t $DOCKER_USERNAME/recharge-payment-service:latest .
cd ..

# 6. Build and Tag Notification Service
cd notification-service
docker build -t $DOCKER_USERNAME/recharge-notification-service:latest .
cd ..

# 7. Build and Tag Recharge Service
cd recharge-service
docker build -t $DOCKER_USERNAME/recharge-recharge-service:latest .
cd ..

echo "✅ Build Complete! To upload to Cloud, run: 'docker login' and then 'docker push -a $DOCKER_USERNAME'"
