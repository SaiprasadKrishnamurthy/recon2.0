FROM golang:1.18 as builder
# Define build env
ENV GOOS linux
ENV CGO_ENABLED 0
ENV GIN_MODE release
# Add a work directory
WORKDIR /app
# Cache and install dependencies
COPY go.mod go.sum ./
RUN go mod download
# Copy app files
COPY . .
# Build app
RUN go build -o app

FROM alpine:3.14 as production
# Add certificates
RUN apk add --no-cache ca-certificates
# Copy built binary from builder
COPY --from=builder app .

RUN mkdir -p /logs
VOLUME /logs

# Expose port
EXPOSE 38080
# Exec built binary
CMD ./app