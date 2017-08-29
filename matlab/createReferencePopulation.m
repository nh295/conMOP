function createReferencePopulation(jarpath, filepath, saveFileName)
%this function creates a reference nondominated population from the
%population files resulting from the search.

try
    conMOP_init(jarpath);
    origin = cd(filepath);
    files = dir('*.pop');
    h = waitbar(0, 'Processing populations...');
    refPop = org.moeaframework.core.NondominatedPopulation;
    for i=1:length(files)
        pop = org.moeaframework.core.PopulationIO.read(java.io.File(files(i).name));
        refPop.addAll(pop);
        waitbar(i/length(files), h);
    end
    
    org.moeaframework.core.PopulationIO.write(...
        java.io.File(strcat(saveFileName,'.pop')), refPop);
    org.moeaframework.core.PopulationIO.writeObjectives(...
        java.io.File(strcat(saveFileName,'.obj')), refPop);
catch me
    close(h)
    clear refPop pop
    fprintf(me.message)
    cd(origin)
    conMOP_end(jarpath);
    disp(me.message);
end
close(h)
clear refPop pop
cd(origin)
conMOP_end(jarpath);

end