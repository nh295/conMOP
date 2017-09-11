%Takes n random final populations and plots them out to show how final
%populations from two methods differ

n_representatives = 2;
path = '/Users/nozomihitomi/Dropbox/conMOP/results/';
method1 = 'static/';
method2 = 'variable/';

files1 = dir(strcat(path,method1,'*.obj'));
ind1 = randi(length(files1),n_representatives);

files2 = dir(strcat(path,method2,'*.obj'));
ind2 = randi(length(files2),n_representatives);

%load refPop to show Pareto front
% load(strcat(path,filesep,'analysis',filesep,'pop_final',filesep,'refPop.mat'));
% [~,ind] = sort(objectives(:,1));

colors = {
    [0         0.4470    0.7410]
    [0.8500    0.3250    0.0980]
    [0.9290    0.6940    0.1250]
    [0.4940    0.1840    0.5560]
    [0.4660    0.6740    0.1880]
    [0.3010    0.7450    0.9330]
    [0.6350    0.0780    0.1840]};

markers = {'o','+','s','>'};

figure(1)
for i=1:n_representatives
    subplot(1,n_representatives,i);
    pop1 = dlmread(strcat(path,method1,files1(ind1(i)).name));
    pop2 = dlmread(strcat(path,method2,files2(ind2(i)).name));
    scatter3(pop1(:,1)/60,pop1(:,2),pop1(:,3)/1000,20,colors{1}, markers{1})
    hold on
    scatter3(pop2(:,1)/60,pop2(:,2),pop2(:,3)/1000,20,colors{2}, markers{2})
    %     plot(-objectives(ind,1), objectives(ind,2),'--k');
    hold off
    xlabel('Average revisit time [min]')
    ylabel('Number of satellites')
    zlabel('Average semi-major axis [km]')
    xlim([0,1500])
    ylim([0,20])
    zlim([6600,7400])
    set(gca,'FontSize',16);
end


subplot(1,n_representatives,1)
legend( 'Static-length chromosome','Variable-length chromosome','location','northwest')
