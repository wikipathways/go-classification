/**
 * AppServicePortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.sdsc.nbcr.opal;

public interface AppServicePortType extends java.rmi.Remote {
    public edu.sdsc.nbcr.opal.types.AppMetadataType getAppMetadata(edu.sdsc.nbcr.opal.types.AppMetadataInputType getAppMetadataInput) throws java.rmi.RemoteException, edu.sdsc.nbcr.opal.types.FaultType;
    public edu.sdsc.nbcr.opal.types.AppConfigType getAppConfig(edu.sdsc.nbcr.opal.types.AppConfigInputType getAppConfigInput) throws java.rmi.RemoteException, edu.sdsc.nbcr.opal.types.FaultType;
    public edu.sdsc.nbcr.opal.types.JobSubOutputType launchJob(edu.sdsc.nbcr.opal.types.JobInputType launchJobInput) throws java.rmi.RemoteException, edu.sdsc.nbcr.opal.types.FaultType;
    public edu.sdsc.nbcr.opal.types.BlockingOutputType launchJobBlocking(edu.sdsc.nbcr.opal.types.JobInputType launchJobBlockingInput) throws java.rmi.RemoteException, edu.sdsc.nbcr.opal.types.FaultType;
    public edu.sdsc.nbcr.opal.types.StatusOutputType queryStatus(java.lang.String queryStatusInput) throws java.rmi.RemoteException, edu.sdsc.nbcr.opal.types.FaultType;
    public edu.sdsc.nbcr.opal.types.JobOutputType getOutputs(java.lang.String getOutputsInput) throws java.rmi.RemoteException, edu.sdsc.nbcr.opal.types.FaultType;
    public byte[] getOutputAsBase64ByName(edu.sdsc.nbcr.opal.types.OutputsByNameInputType getOutputAsBase64ByNameInput) throws java.rmi.RemoteException, edu.sdsc.nbcr.opal.types.FaultType;
    public edu.sdsc.nbcr.opal.types.StatusOutputType destroy(java.lang.String destroyInput) throws java.rmi.RemoteException, edu.sdsc.nbcr.opal.types.FaultType;
}
