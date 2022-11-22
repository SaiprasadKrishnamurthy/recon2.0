package middleware

import (
	"github.com/gin-gonic/gin"
)

func Auth(c *gin.Context) {
	apiKey := c.Request.Header["X-API-KEY"]
	clientKey := c.Request.Header["X-CLIENT-KEY"]

	if len(apiKey) == 0 || len(clientKey) == 0 {
		respondWithError(c, 401, "X-API-KEY and  X-CLIENT-KEY are required")
		return
	} else {
		// Get header. [X-API-KEY, X-CLIENT-KEY]
		// Get API user from Mongo using the 2 Tokens.
		// Get tenant ID.
		// Set it.
	}
	c.Set("tenant", "taxreco")
	c.Next()
}

func respondWithError(c *gin.Context, code int, message interface{}) {
	c.AbortWithStatusJSON(code, gin.H{"error": message})
}
