


<form action="{{ route('upload.csv') }}" method="POST " enctype="multipart/form-data">
    @csrf
    <input type ="file" name="csvFile">
    <button type="submit"> Upload CSV</button>
</form>