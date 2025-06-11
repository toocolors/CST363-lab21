// lab21.js
// Fills pharmacy and drug with test values.

// Drop database
db.doctor.drop();
db.patient.drop();
db.prescription.drop();
db.pharmacy.drop();
db.drug.drop();

// Fill doctor collection
db.doctor.insertOne({ _id: 1, lastName: 'Spock', firstName: 'Schn', practice_since_year: '2000', ssn: '96385741', _class: 'lab21.model.Doctor' });

// Fill pharmacy collection
db.pharmacy.insertOne({ _id: 1, name: 'Jon Doemacy', address: '12345 Joe Doulivard', phone: '566-363-6229',
	drugCosts: [{drugName: 'ethanol', cost: 10 }]});
db.pharmacy.insertOne({ _id: 2, name: 'Salud', address: '98765 Calle de Salud', phone: '666-343-4373',
	drugCosts: [{drugName: 'dihydrogen monoxide', cost: 1}]});

// Fill drug collection
db.drug.insertOne({ _id: 1, name: 'ethanol' });
db.drug.insertOne({ _id: 2, name: 'dihydrogen monoxide' });

// Print collections for debugging
print('pharmacy collection:');
const pharCursor = db.pharmacy.find();
while(pharCursor.hasNext()) {
	let phar = pharCursor.next();
	print(phar);
}

print('\ndrug collection:');
const drugCursor = db.drug.find();
while(drugCursor.hasNext()) {
	let drug = drugCursor.next();
	print(drug);
}