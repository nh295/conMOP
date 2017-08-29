function conMOP_init(path)
%imports the jar files from the matlab directory

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%Add the java class path for the eoss jar file
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
jarfiles = {[path,filesep,'target',filesep,'conMOP-1.0-SNAPSHOT.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'MOEAFramework-2.12.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'orekit-1.0-SNAPSHOT.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'orekit-9.0.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-clustering-1.1.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-core-1.1.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-fft-1.1.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-fitting-1.1.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-geometry-1.1.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-migration-1.1.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-ode-1.1.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-optim-1.1.jar'];
    [path,filesep,'matlab',filesep,'lib',filesep,'hipparchus-stat-1.1.jar'];
    };
tmp = javaclasspath;
javaclasspathadded = false(length(jarfiles));

%search through current dynamics paths to see if jar file is already in
%dynamic path
for i=1:length(tmp)
    for j=1:length(jarfiles)
        if ~isempty(strfind(tmp{i},jarfiles{j}))
            javaclasspathadded(j) = true;
        end
    end
end

for j=1:length(jarfiles)
    if ~javaclasspathadded(j)
        javaaddpath(jarfiles{j})
    end
end

%initialize resources required for orekit
seak.orekit.util.OrekitConfig.init(path)
org.orekit.data.DataProvidersManager.getInstance.addDefaultProviders
