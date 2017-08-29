function creditAnalysis(mode)

selectors = {''};
creditName = {''};
% path = 'C:\Users\SEAK2\Nozomi\EOSS\problems\climateCentric\';
path = '/Users/nozomihitomi/Dropbox/EOSS/problems/climateCentric/';
respath = strcat(path,'result/AIAA SciTech/5000eval_learning_withSinglecross');
origin = cd(respath);

% %morecrossNoInter
% ops = {'OnePointCrossover+AddSynergy+BitFlip','OnePointCrossover+RemoveRandomFromLoadedSatellite+BitFlip',...
%     'OnePointCrossover+RemoveSuperfluous+BitFlip','OnePointCrossover+ImproveOrbit+BitFlip',...
%     'OnePointCrossover+AddRandomToSmallSatellite+BitFlip'...
%     'OnePointCrossover+BitFlip'};
% nops = length(ops);
% labels = {'addSynergyX','removeRandX', 'removeSuperX','improveOrbitX','addSmallX','X'};


%morecrossNoInterNoSingle
ops = {'OnePointCrossover+BitFlip'};
nops = length(ops);
labels = {'Single Point Crossover'};

switch mode
    case 1 %read in the credit csv files
        nFiles =length(selectors)*length(creditName);
        filesProcessed = 0;
        h = waitbar(filesProcessed/nFiles,'Processing files...');
        for b=1:length(selectors)
            filesProcessed = filesProcessed + 1;
            waitbar(filesProcessed/nFiles);
            for c=1:length(creditName)
                fileType =strcat(selectors{b},'*', creditName{c},'*.credit');
                files = dir(fileType);
                allcredits  = cell(length(files),1);
                for i=1:length(files)
                    expData = java.util.HashMap;
                    fid = fopen(files(i).name,'r');
                    while(feof(fid)==0)
                        raw_iteration = strsplit(fgetl(fid),',');
                        %need to split out the operator name
                        line = fgetl(fid);
                        [startIndex, endIndex] = regexp(line,'EOSSOperator.*BitFlip,');
                        raw_credits = strsplit(line(endIndex:end),',');
                        op_data = zeros(length(raw_iteration)-1,2);
                        for j=2:length(raw_credits)
                            op_data(j,1)=str2double(raw_iteration{j}); %iteration
                            op_data(j,2)=str2double(raw_credits{j}); %credit
                        end
                        expData.put(line(startIndex:endIndex),op_data);
                    end
                    fclose(fid);
                    allcredits{i} = expData;
                end
                %save files
                save(strcat(selectors{b},'_',creditName{c},'credit.mat'),'allcredits');
            end
        end
        close(h)
        cd(origin);
        
    case 2 %analyze the files. mode 1 already assumed run
        nepochs = 50;
        maxEval = 5000;
        epochLength = maxEval/nepochs;
        nFiles = length(selectors)*length(creditName);
        filesProcessed = 0;
        h = waitbar(filesProcessed/nFiles,'Processing files...');
        for b=1:length(selectors)
            for c=1:length(creditName)
                filesProcessed = filesProcessed + 1;
                waitbar(filesProcessed/nFiles);
                load(strcat(selectors{b},'_',creditName{c},'credit.mat'));
                eraCreditsAllOp = java.util.HashMap;
                eraCreditVel = java.util.HashMap;
                eraSelectionFreq = java.util.HashMap;
                for i=1:length(allcredits)
                    iter = allcredits{i}.keySet.iterator;
                    totalEpochSelection = zeros(nepochs,1);
                    epochSelectionFreq = java.util.HashMap;
                    rawEraCredits = java.util.HashMap;
                    while iter.hasNext
                        operator = iter.next;
                        data = allcredits{i}.get(operator);
                        ind = isnan(data(:,2));
                        data(ind,2) = 0;
                        eraCreditOneOp = zeros(nepochs,1);
                        eraSelectionOneOp = zeros(nepochs,1);
                        for j=1:nepochs
                            %find indices that lie within epoch
                            ind1 = epochLength*(j-1)<data(:,1);
                            ind2 = data(:,1)<epochLength*j;
                            epoch = data(and(ind1,ind2),:);
                            if(~isempty(epoch(:,1))) %if it is empty then operator was not selected in the epoch
                                epochCredits = epoch(:,2);
                                eraCreditOneOp(j)=mean(epochCredits);
                                %count the unique iterations for which the
                                %operator gets a reward. This corresponds
                                %to when it was selected
                                iters = unique(epoch(:,1));
                                eraSelectionOneOp(j) = length(iters); %number of times this operator was selected in this era
                                totalEpochSelection(j) = totalEpochSelection(j) + eraSelectionOneOp(j); %number of selections in this era
                            end
                        end
                        rawEraCredits.put(operator,eraCreditOneOp);
                        epochSelectionFreq.put(operator,eraSelectionOneOp);
                        
                    end
                    iter = allcredits{i}.keySet.iterator;
                    while iter.hasNext
                        operator = iter.next;
                        %normalize credits
                        normEraCreditOneOp = rawEraCredits.get(operator);
                        %                             normEraCreditOneOp = (rawEraCredits.get(operator)-minCredit)/(maxCredit-minCredit);
                        if isempty(eraCreditsAllOp.get(operator))
                            eraCreditsAllOp.put(operator,normEraCreditOneOp);
                            eraCreditVel.put(operator,diff(normEraCreditOneOp));
                        else
                            eraCreditsAllOp.put(operator,eraCreditsAllOp.get(operator)+normEraCreditOneOp);
                            eraCreditVel.put(operator,eraCreditVel.get(operator)+diff(normEraCreditOneOp));
                        end
                        %normalize seleciton to be ratio
                        freqOperatorSelected = epochSelectionFreq.get(operator)./totalEpochSelection;
                        if isempty(eraSelectionFreq.get(operator))
                            eraSelectionFreq.put(operator,freqOperatorSelected);
                        else
                            eraSelectionFreq.put(operator,eraSelectionFreq.get(operator)+freqOperatorSelected);
                        end
                    end
                end
                %take the average over the number of trials
                subtitle = 'Selection rate = \n';
                cred = zeros(nepochs,allcredits{i}.keySet.size);
                credVel = zeros(nepochs-1,allcredits{i}.keySet.size);
                sel = zeros(nepochs,allcredits{i}.keySet.size);
                for i=1:nops
                    operator = ops{i};
                    %take the average over the trials
                    eraCreditsAllOp.put(operator,eraCreditsAllOp.get(operator)/length(allcredits));
                    eraCreditVel.put(operator,eraCreditVel.get(operator)/length(allcredits));
                    eraSelectionFreq.put(operator,eraSelectionFreq.get(operator)/length(allcredits));
                    
                    subtitle = strcat(subtitle,operator,':\t',sprintf('%.4f',mean(eraSelectionFreq.get(operator))),'\n');
                    cred(:,i) = eraCreditsAllOp.get(operator);
                    credVel(:,i) = eraCreditVel.get(operator);
                    sel(:,i) = eraSelectionFreq.get(operator);
                end
                
                
                h1=figure(1);
                subplot(1,nFiles,filesProcessed)
                plot(cred(2:end,:));
                save(strcat(selectors{b},'_',creditName{c},'_credit','.mat'),'cred');
                legend(labels)
                xlabel('Epoch')
                ylabel('Average credits earned in epoch')
                title(strcat(creditName{c},' credit'))
                
                h2=figure(2);
                subplot(1,nFiles,filesProcessed)
                plot(abs(credVel));
                save(strcat(selectors{b},'_',creditName{c},'_creditVel','.mat'),'credVel');
                xlabel('Epoch')
                ylabel('Speed in the change of the average credits earned in epoch')
                legend(labels)
                title(strcat(creditName{c},' velocity'))
                
                h3=figure(3);
                subplot(1,nFiles,filesProcessed)
                area(sel);
                save(strcat(selectors{b},'_',creditName{c},'_sel','.mat'),'sel');
                axis([0,nepochs,0,1.5])
                xlabel('Epoch')
                ylabel('Average rate of selection in epoch')
                legend(labels)
                title(strcat(creditName{c},' select'))
                %create textbox that shows selection frequency
                %                     t = annotation('textbox');
                %                     t.String = sprintf(subtitle);
                %                     t.Position=[0.1500    0.67    0.3589    0.2333];
                %                     t.HorizontalAlignment = 'right';
                clear allcredits;
            end
        end
        close(h)
        
        plotName = strcat(selectors{b},'learning_',creditName{c});
        saveas(h1,strcat(plotName,'_credit'),'fig');
        saveas(h1,strcat(plotName,'_credit'),'jpeg');
        saveas(h2,strcat(plotName,'_velocity'),'fig');
        saveas(h2,strcat(plotName,'_velocity'),'jpeg');
        saveas(h3,strcat(plotName,'_select'),'fig');
        saveas(h3,strcat(plotName,'_select'),'jpeg');
        clf(h1)
        clf(h2)
        clf(h3)
        
        cd(origin)
end
