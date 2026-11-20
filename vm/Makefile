FILE=vm

all: doc

doc:
	cweave $(FILE)
	tex $(FILE)
	tex $(FILE)
	dvipdfmx $(FILE).dvi

code:
	ctangle $(FILE)