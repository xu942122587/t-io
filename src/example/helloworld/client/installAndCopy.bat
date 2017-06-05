call mvn clean install

call rd ..\..\..\..\dist\examples\helloworld\client /s /q
call xcopy target\dist\tio-examples-helloworld-client-1.7.1.v20170604-RELEASE ..\..\..\..\dist\examples\helloworld\client\ /s /e /q /y

