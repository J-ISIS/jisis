package jisisgroovie;

import java.text.SimpleDateFormat;
import java.util.Date;

def AktuellesDatum() {

def date = new Date();
def sdf = new SimpleDateFormat("yyyyMMddhhmmss");
return sdf.format(date);
}
AktuellesDatum()
