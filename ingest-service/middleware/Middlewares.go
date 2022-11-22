package middleware

import (
	"context"

	"github.com/gin-gonic/gin"
	"github.com/saiprasadkrishnamurthy/ingest-service/entity"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
)

func CORSMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
		c.Writer.Header().Set("Access-Control-Allow-Credentials", "true")
		c.Writer.Header().Set("Access-Control-Allow-Headers", "Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization, accept, origin, Cache-Control, X-Requested-With")
		c.Writer.Header().Set("Access-Control-Allow-Methods", "POST, OPTIONS, GET, PUT")

		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}

		c.Next()
	}
}

func Auth(mongoClient *mongo.Client) gin.HandlerFunc {
	fn := func(c *gin.Context) {
		apiKey := c.Request.Header["X-Api-Key"]
		clientId := c.Request.Header["X-Client-Id"]
		if len(apiKey) == 0 || len(clientId) == 0 {
			respondWithError(c, 401, "X-API-KEY and  X-CLIENT-ID are required")
			return
		} else {
			// Get header. [X-API-KEY, X-CLIENT-KEY]
			var apiUser entity.ApiUser
			apiUsers := mongoClient.Database("users").Collection("apiUsers")
			filter := bson.D{{Key: "clientid", Value: clientId[0]},
				{Key: "apikey", Value: apiKey[0]}}
			apiUsers.FindOne(context.TODO(), filter).Decode(&apiUser)
			if len(apiUser.TenantId) == 0 {
				respondWithError(c, 401, "Invalid Credentials")
				return
			}
			c.Set("tenant", apiUser.TenantId)
		}
		c.Next()
	}
	return gin.HandlerFunc(fn)
}

func respondWithError(c *gin.Context, code int, message interface{}) {
	c.AbortWithStatusJSON(code, gin.H{"error": message})
}
