PROYECT_NAME = MailCopier
MAIN_CLASS = view.Main
DEBUG = true

CP = '.:assets/lib/*:bin'
#windows:
CP2 = '.;assets/lib/*;bin'

COPY_ASSETS = mkdir dist/assets; cp -r assets/images dist/assets/images; cp -r assets/lib dist/assets/lib; cp -r assets/themes dist/assets/themes; #cp assets/README.txt dist/README.txt; cp assets/LEEME.txt dist/LEEME.txt

JAVAC = javac -classpath $(CP) -d bin
JAR = jar cvmf assets/Manifest.mf dist/$(PROYECT_NAME).jar -C bin .
PROGUARD = java -jar ~/jars/proguard.jar @assets/config.pro

.PHONY: Main Proguard Rclass Rjar Mclass Mjar CLEAN CLASS_CLEAN JAR_CLEAN

Main: Mclass Rclass

Proguard:
	$(PROGUARD)

Rclass: 
	java -cp $(CP) $(MAIN_CLASS) 

Rjar: 
	cd dist;java -jar $(PROYECT_NAME).jar

Mjar: dist JAR_CLEAN Mclass
	$(JAR)
	$(COPY_ASSETS)

Mclass: bin CLASS_CLEAN
	cp -r -t bin/ ./src/*
	find bin|grep '.java'|xargs rm
	find src|grep '.java'|xargs $(JAVAC) 

CLASS_CLEAN:
	rm -r bin/*; true

JAR_CLEAN:
	rm -r dist/*;true

CLEAN: CLASS_CLEAN JAR_CLEAN

bin:
	mkdir bin

dist:
	mkdir dist
