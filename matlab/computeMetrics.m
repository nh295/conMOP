function [NFE, HV, IGD] = computeMetrics(root,filepath)
%This function computes the metrics based on the solution history stored in
%the datafile. The hypervolume and inverted generational distance are
%computed based on the reference population. Step determines how often to
%compute these metrics

refPopPath = strcat(root, filesep, 'results', filesep, 'refpop', filesep, 'refPop.pop');
maxNFE = 5000;
step = 5;
epsilonDouble = [10, 1, 1000];
h = waitbar(0, 'Processing populations...');
try
    conMOP_init(root);
    origin = cd(filepath);
    files = dir('*.pop');
    NFE = zeros(maxNFE/step,length(files));
    HV = zeros(maxNFE/step,length(files));
    IGD = zeros(maxNFE/step,length(files));
    
    refPop = org.moeaframework.core.PopulationIO.read(java.io.File(refPopPath));
    refPop = org.moeaframework.core.NondominatedPopulation(refPop);
    
    refObj = dlmread(strcat(root, filesep, 'results', filesep, 'refpop', filesep, 'refPop.obj'));
    
    %initialize problem
    prob = seak.conmop.ConstellationOptimizer();
    refPoint = max(refObj)*1.1;
    fhv = org.moeaframework.core.indicator.Hypervolume(prob, refPop, refPoint);
    igd = org.moeaframework.core.indicator.InvertedGenerationalDistance(prob, refPop);
    
    for i=1:length(files)
        
        population = org.moeaframework.core.PopulationIO.read(java.io.File(files(i).name));
        map = java.util.HashMap;
        iter = population.iterator();
        %sort solutions by NFE
        while(iter.hasNext())
            sltn = iter.next;
            sltnNFE = sltn.getAttribute('NFE');
            if(~map.containsKey(sltnNFE))
                map.put(sltnNFE,java.util.ArrayList);
            end
            map.get(sltnNFE).add(sltn);
        end
        
        nfeList = java.util.ArrayList(map.keySet());
        java.util.Collections.sort(nfeList);
        
        %go over the sorted nfe
        archive = org.moeaframework.core.EpsilonBoxDominanceArchive(epsilonDouble);
        currentNFE = 0;
        k = 1;
        for j=0:step:maxNFE
            while (currentNFE < nfeList.size() && nfeList.get(currentNFE) <= j)
                archive.addAll(map.get(nfeList.get(currentNFE)));
                currentNFE = currentNFE + 1;
            end
            NFE(k,i) = j;
            HV(k,i) = fhv.evaluate(archive);
            IGD(k,i) = igd.evaluate(archive);
            k = k + 1;
        end
        
        waitbar(i/length(files), h);
    end
catch me
    cd(origin)
    clear refPop refPoint iter map population archive fhv igd prob sltn
    conMOP_end(root);
    disp(me.message);
    close(h)
end
close(h)
clear refPop refPoint iter map population archive fhv igd prob sltn
cd(origin)
conMOP_end(root);

end