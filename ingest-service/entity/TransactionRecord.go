package entity

type TransactionRecord struct {
	ID    string                 `bson:"_id,omitempty"`
	Name  string                 `bson:"name,omitempty"`
	Attrs map[string]interface{} `bson:"attrs,omitempty"`
}
