package model

import "log"

type Log struct {
	Warn  *log.Logger
	Info  *log.Logger
	Error *log.Logger
}
