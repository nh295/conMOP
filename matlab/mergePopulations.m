function mergePopulations(jarpath,filepath)
%this function merges all results into one mat file containing the relevant
%information for each solution

try
    conMOP_init(jarpath);
    origin = cd(filepath);
    files = dir('*.pop');
    mergedPopulation = org.moeaframework.core.Population;
    h = waitbar(0, 'Processing populations...');
    for i=1:length(files)
        mergedPopulation.addAll(...
            org.moeaframework.core.PopulationIO.read(...
            java.io.File(files(i).name)));
        waitbar(i/length(files), h);
    end
    close(h);
    
    iter = mergedPopulation.iterator;
    popSize = mergedPopulation.size;
    objectives = zeros(popSize,3);
    nfe = zeros(popSize,1);
    
    i = 1;
    h = waitbar(0, 'Processing solutions...');
    while(iter.hasNext)
        solution = iter.next;
%         for j=0:solution.getNumberOfVariables-1
%             try
%                 decisions(i,j+1) = solution.getVariable(j).get(0);
%             catch
%                 decisions(i,j+1) = solution.getVariable(j).getValue();
%             end
%         end
        objectives(i,:) = solution.getObjectives();
        nfe(i) = solution.getAttribute('NFE');
        i = i+1;
        waitbar(i/popSize, h);
    end
    close(h)
    
catch me
    fprintf(me.message)
    clear solution iter mergedPopulation h origin ans filepath files
    cd(origin)
    conMOP_end(jarpath);
    disp(me.message);
end
cd(origin)
clear solution iter mergedPopulation h origin ans filepath files
conMOP_end(jarpath);

save data.mat

