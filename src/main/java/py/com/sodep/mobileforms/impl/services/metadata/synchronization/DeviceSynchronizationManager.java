package py.com.sodep.mobileforms.impl.services.metadata.synchronization;

import py.com.sodep.mobileforms.api.entities.application.Application;

public interface DeviceSynchronizationManager {

	//1) The device will download network configuration
	//Currently we will include the synchronization period (when will the application retry)
	
	//2) When something has changed
	//Inform that an application has changed and somebody might be interesting on it
	//for every device that is interested register a synch request (or send a push notification if its possible)
	//This is almost the same method used in the synchronization process coming from a device
	//
	//3) synchronization process of the device
	//the whole data set is sent to the device (do not check synch flag, just send all data). We are assuming that if the device has contact the server, is because its needs data
	//The device should be clever enough to re-try the synchronization process if he couldn't download data
	//
	 public void applicationChanged(Application app);
	 
	 
	 //Device
	 //1) Synchronize process when the application starts
	 //Synch all metadata
	 //synch lookuptables definition
	 //synch lookuptables
	 //2) 
	 
	 //Data Set synchronization
	 //data_budget
	 // "data_operation" : {opID:id,insert, from:x, to:x,metaDataRef:} {opID:id,update, index:3},{opID:id,delete,from:,to:}
	 // "device_tracking": {device:,user:u,lastOperation:opId,completed:true}
	 //El device envia un confirmation del operation que recibio
	 //el tiene un loop y pide lookupTable data hasta que el servidor contesta q no tiene mas
	 //el device dice que operacion recibio, y el server le vuelve a enviar mas datos
	 //cuando el server pasa a un siguiente lookupTable o cuando ya no hay datos entonces se envia un "confirm"
	 //el transaction ID se inicia en el device cuando el server empieza enviar datos de un lookupTable
	 
	 
}
