%analyze data from orekit conMop run

%set up parameters
maxSat = 10;
kclusters = 10;

%load population
file = java.io.File('variable_test0_all.pop');

conMOP_init(cd)
pop = org.moeaframework.core.PopulationIO.read(file);
ndpop = org.moeaframework.core.NondominatedPopulation(pop);

%collect orbital elements of each satellite in each constellation
oe = cell(pop.size(),1);
ninstallments = zeros(pop.size(),1);
stdinc = zeros(pop.size(),1);
uniqueRAAN = zeros(pop.size(),1);
for i = 0 : pop.size-1
    sats = pop.get(i).getVariable(0).getSatelliteVariables();
    m = zeros(sats.size,6);
    for j=0:sats.size-1
        m(j+1,1) = sats.get(j).getSma;
        m(j+1,2) = sats.get(j).getEcc;
        m(j+1,3) = sats.get(j).getInc;
        m(j+1,4) = sats.get(j).getRaan;
        m(j+1,5) = sats.get(j).getArgPer;
        m(j+1,6) = sats.get(j).getTrueAnomaly;
    end
    oe{i+1} = m;
    stdinc(i+1) = std(m(:,3));
    uniqueRAAN(i+1) = length(unique(m(:,4)));
%     ninstallments(i+1) = pop.get(i).getVariable(0).getDeploymentStrategy.getInstallments.size();
end

nsolns = pop.size;

nobj = pop.get(0).getNumberOfObjectives;

nsats = zeros(nsolns,1);
popObj = zeros(nsolns,nobj);
nfe = zeros(nsolns,1)-1;
for i=1:nsolns
    nsats(i) = size(oe{i},1);
    nfe(i) = pop.get(i-1).getAttribute("NFE");
    popObj(i,:) = pop.get(i-1).getObjectives;
end

ndObj = zeros(ndpop.size,nobj);
ndNfe = zeros(ndpop.size,1);
for i=1:ndpop.size
    ndObj(i,:) = ndpop.get(i-1).getObjectives;
    ndNfe(i) = ndpop.get(i-1).getAttribute("NFE");
end
quality = false(nsolns,1);

clear ndpop pop file sats
conMOP_end(cd)

%record the indices to label as behavioral
for i=1:nsolns
    for j=1:size(ndObj,1)
        if sum(ndObj(j,:) == popObj(i,:)) == nobj
            quality(i) = true;
            break
        end
    end
end

%put all orbital elements into one big matrix
allSats = zeros(nsolns*maxSat,8);
sat_i = 1;
for i=1:length(oe)
    m = oe{i};
    for j=1:size(m,1)
        allSats(sat_i,:) = [m(j,:),i,quality(i)];
        sat_i = sat_i+1;
    end
end
allSats(sat_i:end,:) = [];

%identify clusters in sma, inc, and raan
csvwrite('test.csv',allSats(:,1));
system(sprintf('Rscript ./r/ckmeans_1d_dp.r test.csv 1 %d',kclusters));
sma_clusters = csvread('test_cluster.csv',1,0);
k = unique(sma_clusters);
sma_ranges = zeros(length(k),2);
for i=1:length(k)
    ind = sma_clusters == k(i);
    sma_ranges(i,:) = [min(allSats(ind,1)),max(allSats(ind,1))];
end
csvwrite('test.csv',allSats(:,3));
system(sprintf('Rscript ./r/ckmeans_1d_dp.r test.csv 1 %d',kclusters));
inc_clusters = csvread('test_cluster.csv',1,0);
k = unique(inc_clusters);
inc_ranges = zeros(length(k),2);
for i=1:length(k)
    ind = inc_clusters == k(i);
    inc_ranges(i,:) = [min(allSats(ind,3)),max(allSats(ind,3))];
end
csvwrite('test.csv',allSats(:,4));
system(sprintf('Rscript ./r/ckmeans_1d_dp.r test.csv 1 %d',kclusters));
raan_clusters = csvread('test_cluster.csv',1,0);
k = unique(raan_clusters);
raan_ranges = zeros(length(k),2);
for i=1:length(k)
    ind = raan_clusters == k(i);
    raan_ranges(i,:) = [min(allSats(ind,4)),max(allSats(ind,4))];
end

%count the number of satellites in each oe range for each satellite
feats_sma = zeros(nsolns, size(sma_ranges,1));
feats_inc = zeros(nsolns, size(inc_ranges,1));
feats_raan = zeros(nsolns, size(raan_ranges,1));
for j=1:size(sma_ranges,1)
    for i =1:length(oe)
        constel = oe{i};
        feats_sma(i,j) = ...
            sum(and(constel(:,1) > sma_ranges(j,1),constel(:,1) < sma_ranges(j,2)));
    end
end
for j=1:size(inc_ranges,1)
    for i =1:length(oe)
        constel = oe{i};
        feats_inc(i,j) = ...
            sum(and(constel(:,3) > inc_ranges(j,1),constel(:,3) < inc_ranges(j,2)));
    end
end
for j=1:size(raan_ranges,1)
    for i =1:length(oe)
        constel = oe{i};
        feats_raan(i,j) = ...
            sum(and(constel(:,4) > raan_ranges(j,1),constel(:,4) < raan_ranges(j,2)));
    end
end

%count the number of sma, inc, and raan clusters are present in each
%solution
nSmas = zeros(nsolns,1);
nIncs = zeros(nsolns,1);
nRaans = zeros(nsolns,1);
for i=1:nsolns
    nSmas(i) = length(unique(feats_sma(i,1:size(sma_ranges,1))));
    nIncs(i) = length(unique(feats_inc(i,1:size(inc_ranges,1))));
    nRaans(i) = length(unique(feats_raan(i,1:size(raan_ranges,1))));
end

oe_var = zeros(nsolns,6);
oe_mean = zeros(nsolns,6);
for i=1:nsolns
constel = oe{i};
oe_var(i,:) = var(constel,1);
oe_mean(i,:) = mean(constel,1);
end

clear sat_i i j k m constel maxSat nobj kclusters ans