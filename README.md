# XpathGenerator
Make an exhaustive list of Xpath expressions for nodes in an XML document

## Usage

java -jar XpathGenerator.jar [-p] [-i] \<inputfile\> [\<contextfile\>]

### Parameters
| Parameter | Required? | Description |
| --------- | --------- | ----------- |
| -i | optional | print the xpath predicate index number 1..n |
| -p | optional | print the value at the xpath |
| inputfile | mandatory | well formed xml input file |
| contextfile | optional | text file containing xml namespace declarations eg xmlns:fhir="http://hl7.org/fhir" |
