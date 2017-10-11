%runs the post-run analysis
%reads .res file and puts all results data into one mfile

path = '/Users/nozomihitomi/Dropbox/conMOP/results/metrics/';
load(strcat(path,'static.mat'));

nexperiments = 2;
ntrials = 30;
hv = zeros(nexperiments,size(HV,1),ntrials);
igd = zeros(nexperiments,size(IGD,1),ntrials);
hv(1,:,:) = HV(:,1:ntrials);
igd(1,:,:) = IGD(:,1:ntrials);

load(strcat(path,'variable.mat'));
hv(2,:,:) = HV(:,1:ntrials);
igd(2,:,:) = IGD(:,1:ntrials);

dataPoints = size(HV,1);
base_experiment_metric_sigHV = zeros(dataPoints,nexperiments-1);
base_experiment_metric_sigIGD = zeros(dataPoints,nexperiments-1);
sigLevel = 0.05;
for i = 2:nexperiments
    for j=1:dataPoints
        %for hypervolume
        [~,h] = ranksum(squeeze(hv(1,j,:)),squeeze(hv(i,j,:)),'alpha',sigLevel);
        if h==1 %then significant difference and medians are different at 0.95 confidence
            med_diff = median(squeeze(hv(1,j,:)))-median(squeeze(hv(i,j,:)));
            if med_diff < 0
                base_experiment_metric_sigHV(j,i) = -1;
            else
                base_experiment_metric_sigHV(j,i) = 1;
            end
        else
            base_experiment_metric_sigHV(j,i) = 0;
        end
        
        %for inverted generational distance
        [~,h] = ranksum(squeeze(igd(1,j,:)),squeeze(igd(i,j,:)));
        if h==1 %then significant difference and medians are different at 0.95 confidence
            med_diff = median(squeeze(igd(1,j,:)))-median(squeeze(igd(i,j,:)));
            if med_diff < 0
                base_experiment_metric_sigIGD(j,i) = -1;
            else
                base_experiment_metric_sigIGD(j,i) = 1;
            end
        else
            base_experiment_metric_sigIGD(j,i) = 0;
        end
    end
end

%plot standard dev areas
 colors = {
    [0         0.4470    0.7410]
    [0.8500    0.3250    0.0980]
    [0.9290    0.6940    0.1250]
    [0.4940    0.1840    0.5560]
    [0.4660    0.6740    0.1880]
    [0.3010    0.7450    0.9330]
    [0.6350    0.0780    0.1840]};
% mu_hv =  mean(squeeze(hv(1,:,:)),2);
% mu_igd =  mean(squeeze(igd(1,:,:)),2);
mu_hv = zeros(size(hv,2),1);
mu_igd = zeros(size(igd,2),1);

%plot HV history over NFE
figure(1)
cla
hold on
handles = [];
maxHV = max(max(max(hv)));

for i=1:nexperiments
    X = [NFE(:,1);flipud(NFE(:,1))];
    stddev = std(squeeze(hv(i,:,:)),0,2);
    mu = (mean(squeeze(hv(i,:,:)),2)-mu_hv);
    Y = [mu-stddev;flipud(mu+stddev)];
    h = fill(X,Y,colors{i},'EdgeColor','none');
    alpha(h,0.15) %sest transparency
    handles = [handles plot(NFE(:,1), mu, '-', 'Color',colors{i})];
    %plot where the performance is statistically significantly different
    ind = or(base_experiment_metric_sigHV(:,i)==1,base_experiment_metric_sigHV(:,i)==-1);
    plot(NFE(ind,1),mu(ind),'LineStyle','none','Marker','.','MarkerSize', 20,'Color',colors{i});
end
axis([0,10000,0,1.2])
hold off
xlabel('NFE')
ylabel('HV')
legend(handles, 'Fixed-length chromosome', 'Variable-length chromosome','Location','SouthEast')
% legend(handles, 'baseline','100','250','500','1000', 'Location', 'SouthEast')
set(gca,'FontSize',16);

%find convergence differences
thresholdHV = 0.95;
attainment = zeros(nexperiments,ntrials);
for i=1:nexperiments
    for j=1:ntrials
        ind = find(hv(i,:,j) >= thresholdHV*maxHV, 1);
        if isempty(ind)
            attainment(i,j) = inf;
        else
            attainment(i,j) = NFE(ind,1);
        end
    end
end
figure(2)
cla
hold on
for i=1:nexperiments
    ecdf(attainment(i,:))
end
axis([0,10000,0,1])
hold off
xlabel('NFE')
ylabel(sprintf('Probability of attaing %2.2f%% HV',thresholdHV*100))
legend('AOS', '\epsilon-MOEA', 'All', 'Random','Location','NorthEast')
set(gca,'FontSize',16);

%plot IGD history over NFE
figure(3)
cla
hold on
handles = [];

for i=1:nexperiments
    X = [NFE(:,1);flipud(NFE(:,1))];
    stddev = std(squeeze(igd(i,:,:)),0,2);
    mu = (mean(squeeze(igd(i,:,:)),2)-mu_igd);
    Y = [mu-stddev;flipud(mu+stddev)];
    h = fill(X,Y,colors{i},'EdgeColor','none');
    alpha(h,0.15) %sest transparency
    handles = [handles plot(NFE(:,1), mu, '-', 'Color',colors{i})];
    %plot where the performance is statistically significantly different
    ind = or(base_experiment_metric_sigIGD(:,i)==1,base_experiment_metric_sigIGD(:,i)==-1);
    plot(NFE(ind,1),mu(ind),'LineStyle','none','Marker','.','MarkerSize', 20,'Color',colors{i});
end
axis([0,10000,0,2])
hold off
xlabel('NFE')
ylabel('IGD')
legend(handles, 'Fixed-length chromosome', 'Variable-length chromosome','Location','NorthEast')
% legend(handles, 'CPD','DNF','ACH','ACS', 'Location', 'SouthEast')
set(gca,'FontSize',16);

%find convergence differences
rangeIGD = max(max(max(igd)))-min(min(min(igd)));
thresholdIGD = 1-0.75;
attainment = zeros(nexperiments,ntrials);
for i=1:nexperiments
    for j=1:ntrials
        ind = find(igd(i,:,j) <= thresholdIGD*rangeIGD, 1);
        if isempty(ind)
            attainment(i,j) = inf;
        else
            attainment(i,j) = NFE(ind,1);
        end
    end
end
figure(4)
cla
hold on
for i=1:nexperiments
    ecdf(attainment(i,:))
end
axis([0,5000,0,1])
hold off
xlabel('NFE')
ylabel(sprintf('Probability of attaing %2.2f%% IGD',thresholdIGD*100))
legend('AOS', '\epsilon-MOEA', 'All', 'Random','Location','SouthEast')
set(gca,'FontSize',16);